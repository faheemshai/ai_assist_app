# ai_assist_app — Inventory API

Spring Boot microservice deployable on OpenShift using an internal image build.

---

## Prerequisites

| Tool | Purpose |
|------|---------|
| `oc` CLI | OpenShift client — [download](https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/) |
| `mvn` 3.8+ | Build the JAR (only needed if you want to test locally first) |
| Access to an OpenShift cluster | With permission to create projects and build configs |

---

## Deploy to OpenShift — step by step

### 1. Log in to your cluster

```bash
oc login https://<your-cluster-api-url> --token=<your-token>
```

### 2. Create the project (namespace)

```bash
oc apply -f inventory-api/k8s/namespace.yaml
oc project demo-inventory-app
```

### 3. Create the ImageStream and BuildConfig

```bash
oc apply -f inventory-api/k8s/imagestream.yaml
oc apply -f inventory-api/k8s/buildconfig.yaml
```

### 4. Build the image from local source

This streams your local `inventory-api/` directory to the OpenShift build system —
no external registry or Docker daemon required.

```bash
oc start-build inventory-api \
  --from-dir=./inventory-api \
  --follow
```

Wait until the build completes and you see `Push successful`.

### 5. Deploy the app and expose it

```bash
oc apply -f inventory-api/k8s/deployment.yaml
oc apply -f inventory-api/k8s/route.yaml
```

### 6. Verify everything is running

```bash
# Check pod status
oc get pods -n demo-inventory-app

# Get the public URL
oc get route inventory-api -n demo-inventory-app
```

You should see output like:
```
NAME            HOST/PORT                                                    PATH   SERVICES        PORT   ...
inventory-api   inventory-api-demo-inventory-app.apps.<cluster-domain>             inventory-api   http   ...
```

### 7. Smoke-test the API

```bash
export HOST=$(oc get route inventory-api -n demo-inventory-app -o jsonpath='{.spec.host}')

# List items (empty to start)
curl -s https://$HOST/api/items | jq .

# Create an item
curl -s -X POST https://$HOST/api/items \
  -H 'Content-Type: application/json' \
  -d '{"name":"widget","quantity":10}' | jq .
```

---

## Re-deploying after a code change

Just re-trigger the build — the Deployment will automatically roll out the new image:

```bash
oc start-build inventory-api --from-dir=./inventory-api --follow
```

---

## Manifest overview

| File | Purpose |
|------|---------|
| `k8s/namespace.yaml` | Creates the `demo-inventory-app` OpenShift project |
| `k8s/imagestream.yaml` | Internal image registry tag for the built image |
| `k8s/buildconfig.yaml` | Docker-strategy build that reads the local `Dockerfile` |
| `k8s/deployment.yaml` | Deployment + Service (right-sized: 250m/256Mi → 500m/512Mi) |
| `k8s/route.yaml` | Edge-TLS Route — HTTPS with HTTP→HTTPS redirect |
