# Deploying BankFlow to Kubernetes

Tested on the Kubernetes cluster built into Docker Desktop. Everything runs in the `bankflow` namespace.

## 1. Infrastructure

Namespace:

```bash
kubectl apply -f deploy/k8s/namespace.yaml
```

Database credentials. Created by hand, never committed:

```bash
kubectl -n bankflow create secret generic bankflow-db \
  --from-literal=username=postgres \
  --from-literal=password=<choose-a-password>
```

One PostgreSQL per service (database per service), all from the same chart:

```bash
helm upgrade --install auth-db        deploy/helm/postgres -n bankflow --set database=auth_db
helm upgrade --install account-db     deploy/helm/postgres -n bankflow --set database=account_db
helm upgrade --install transaction-db deploy/helm/postgres -n bankflow --set database=transaction_db
helm upgrade --install ledger-db      deploy/helm/postgres -n bankflow --set database=ledger_db
```

Kafka through the [Strimzi](https://strimzi.io) operator. The operator lives in its own namespace and watches `bankflow`:

```bash
helm upgrade --install strimzi oci://quay.io/strimzi-helm/strimzi-kafka-operator \
  --version 1.2.0 -n strimzi --create-namespace \
  --set "watchNamespaces={bankflow}"

kubectl apply -f deploy/k8s/kafka.yaml
kubectl -n bankflow wait kafka/bankflow --for=condition=Ready --timeout=300s
```

Check:

```bash
kubectl -n bankflow get pods
```

Expected: `auth-db-0`, `account-db-0`, `transaction-db-0`, `ledger-db-0` and `bankflow-dual-role-0` (Kafka), all `Running` and `1/1` ready. The operator itself runs in the `strimzi` namespace.

Inside the cluster the services reach the databases at `<release>:5432` (e.g. `auth-db:5432`) and Kafka at `bankflow-kafka-bootstrap:9092`.

## 2. Services

Application secrets. Created by hand, never committed (use the same values as in `.env`; the JWT secret needs at least 32 characters):

```bash
kubectl -n bankflow create secret generic bankflow-app \
  --from-literal=jwt-secret=<jwt-secret> \
  --from-literal=internal-api-key=<internal-api-key>
```

Every service is installed from the same chart with its own values file. Images come from GHCR and are tagged with the git SHA of the commit on `main` that built them:

```bash
TAG=$(git rev-parse origin/main)

helm upgrade --install auth-service        deploy/helm/bankflow-service -n bankflow -f deploy/values/auth-service.yaml        --set image.tag=$TAG
helm upgrade --install account-service     deploy/helm/bankflow-service -n bankflow -f deploy/values/account-service.yaml     --set image.tag=$TAG
helm upgrade --install transaction-service deploy/helm/bankflow-service -n bankflow -f deploy/values/transaction-service.yaml --set image.tag=$TAG
helm upgrade --install ledger-service      deploy/helm/bankflow-service -n bankflow -f deploy/values/ledger-service.yaml      --set image.tag=$TAG
```

Check that all four become `1/1` ready (the startup probe gives each JVM up to three minutes):

```bash
kubectl -n bankflow get pods -w
```

Until the gateway is in place, reach a service with a port-forward, e.g. `kubectl -n bankflow port-forward svc/auth-service 8081:8081` and open http://localhost:8081/swagger-ui.html.

## Removing everything

```bash
helm -n bankflow uninstall auth-service account-service transaction-service ledger-service
helm -n bankflow uninstall auth-db account-db transaction-db ledger-db
kubectl delete -f deploy/k8s/kafka.yaml
helm -n strimzi uninstall strimzi
kubectl delete namespace bankflow strimzi
```

Database volumes (PVCs) are kept by `helm uninstall` and removed with the namespace.