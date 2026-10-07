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

To open a service's Swagger UI, use a port-forward, e.g. `kubectl -n bankflow port-forward svc/auth-service 8081:8081` and http://localhost:8081/swagger-ui.html.

## 3. Gateway

[Envoy Gateway](https://gateway.envoyproxy.io) implements the Kubernetes Gateway API. One Gateway receives all traffic on http://localhost and each service's HTTPRoute (from its values file) forwards its path prefix:

| Path | Service |
|---|---|
| `/api/auth/**` | auth-service |
| `/api/accounts/**` | account-service |
| `/api/transactions/**` | transaction-service |
| `/api/ledger/**` | ledger-service |

`/actuator/**` and `/internal/**` are not routed, so they stay inside the cluster.

```bash
helm upgrade --install eg oci://docker.io/envoyproxy/gateway-helm \
  --version v1.9.2 -n envoy-gateway-system --create-namespace
kubectl -n envoy-gateway-system wait deployment/envoy-gateway --for=condition=Available --timeout=300s

kubectl apply -f deploy/k8s/gateway.yaml
kubectl -n bankflow wait gateway/bankflow --for=condition=Programmed --timeout=300s
```

The routes are part of the service chart, so re-run the four `helm upgrade --install` commands from step 2 after the gateway exists.

Try it:

```bash
curl -X POST http://localhost/api/auth/register -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","firstName":"TestN","lastName":"TestL"}'
```

## 4. Monitoring

[kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack) installs the Prometheus Operator, Prometheus, Grafana, kube-state-metrics and node-exporter. Every BankFlow service ships a `ServiceMonitor` (from the service chart), so Prometheus scrapes `/actuator/prometheus` inside the cluster.

Grafana admin credentials. Created by hand, never committed:

```bash
kubectl create namespace monitoring
kubectl -n monitoring create secret generic grafana-admin \
  --from-literal=admin-user=admin \
  --from-literal=admin-password=<choose-a-password>
```

Install the stack and the BankFlow dashboard:

```bash
helm upgrade --install monitoring oci://ghcr.io/prometheus-community/charts/kube-prometheus-stack \
  --version 92.1.0 -n monitoring -f deploy/monitoring/values.yaml

kubectl apply -f deploy/monitoring/bankflow-dashboard.yaml
```

The `ServiceMonitor`s are rendered only when the Prometheus Operator CRDs exist, so re-run the four `helm upgrade --install` commands from step 2 after installing the stack.

Grafana and Prometheus are internal tools and are not routed through the Gateway; open them with a port-forward:

```bash
kubectl -n monitoring port-forward svc/monitoring-grafana 3000:80                        # http://localhost:3000, dashboard "BankFlow - Services"
kubectl -n monitoring port-forward svc/monitoring-kube-prometheus-prometheus 9090:9090   # http://localhost:9090/targets
```

## 5. Logs

The services log one JSON object per line in the cluster (`LOGGING_STRUCTURED_FORMAT_CONSOLE=logstash`, set by the service chart; local runs keep plain text). [Grafana Alloy](https://grafana.com/docs/alloy/) reads the logs of every pod in `bankflow` through the Kubernetes API and pushes them to [Loki](https://grafana.com/oss/loki/); Grafana queries Loki next to Prometheus.

```bash
helm upgrade --install loki oci://ghcr.io/grafana-community/helm-charts/loki \
  --version 18.13.8 -n monitoring -f deploy/monitoring/loki-values.yaml

helm repo add grafana https://grafana.github.io/helm-charts
helm upgrade --install alloy grafana/alloy \
  --version 1.13.0 -n monitoring -f deploy/monitoring/alloy-values.yaml

# adds the Loki data source to Grafana
helm upgrade --install monitoring oci://ghcr.io/prometheus-community/charts/kube-prometheus-stack \
  --version 92.1.0 -n monitoring -f deploy/monitoring/values.yaml
kubectl apply -f deploy/monitoring/bankflow-dashboard.yaml
```

Re-run the four service `helm upgrade --install` commands from step 2 so the services switch to JSON logs. In Grafana, **Explore → Loki**, for example:

```logql
{namespace="bankflow", level="ERROR"}
{app="ledger-service"} | json | logger_name =~ ".*TransactionEventConsumer"
```

## Removing everything

```bash
helm -n monitoring uninstall alloy loki monitoring
helm -n monitoring uninstall monitoring
helm -n bankflow uninstall auth-service account-service transaction-service ledger-service
helm -n bankflow uninstall auth-db account-db transaction-db ledger-db
kubectl delete -f deploy/k8s/gateway.yaml
helm -n envoy-gateway-system uninstall eg
kubectl delete -f deploy/k8s/kafka.yaml
helm -n strimzi uninstall strimzi
kubectl delete namespace bankflow strimzi envoy-gateway-system monitoring
```

Database volumes (PVCs) are kept by `helm uninstall` and removed with the namespace.