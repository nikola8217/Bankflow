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

## Removing everything

```bash
helm -n bankflow uninstall auth-db account-db transaction-db ledger-db
kubectl delete -f deploy/k8s/kafka.yaml
helm -n strimzi uninstall strimzi
kubectl delete namespace bankflow strimzi
```

Database volumes (PVCs) are kept by `helm uninstall` and removed with the namespace.