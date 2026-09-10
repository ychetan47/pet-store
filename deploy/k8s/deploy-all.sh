#!/usr/bin/env bash
# ==============================================================================
# Paws & Claws Pet Store — GKE (Strategy B) Apply All Manifests
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "================================================================="
echo "☸️  Deploying Paws & Claws Pet Store to GKE (Strategy B)"
echo "================================================================="

echo "1. Applying Namespace..."
kubectl apply -f "${SCRIPT_DIR}/00-namespace.yaml"

echo "2. Applying Secrets & ConfigMap..."
kubectl apply -f "${SCRIPT_DIR}/01-secrets.yaml"
kubectl apply -f "${SCRIPT_DIR}/02-configmap.yaml"

echo "3. Deploying In-Cluster PostgreSQL..."
kubectl apply -f "${SCRIPT_DIR}/03-postgres.yaml"

echo "4. Deploying In-Cluster Kafka..."
kubectl apply -f "${SCRIPT_DIR}/04-kafka.yaml"

echo "Waiting for PostgreSQL & Kafka to be ready..."
kubectl rollout status statefulset/postgres -n petstore --timeout=120s || true
kubectl rollout status statefulset/kafka -n petstore --timeout=120s || true

echo "5. Deploying Backend Microservices..."
kubectl apply -f "${SCRIPT_DIR}/05-user-service.yaml"
kubectl apply -f "${SCRIPT_DIR}/06-catalog-service.yaml"
kubectl apply -f "${SCRIPT_DIR}/07-inventory-service.yaml"
kubectl apply -f "${SCRIPT_DIR}/08-order-service.yaml"
kubectl apply -f "${SCRIPT_DIR}/09-notification-service.yaml"

echo "6. Deploying API Gateway & Frontends..."
kubectl apply -f "${SCRIPT_DIR}/10-api-gateway.yaml"
kubectl apply -f "${SCRIPT_DIR}/11-customer-frontend.yaml"
kubectl apply -f "${SCRIPT_DIR}/12-admin-frontend.yaml"

echo "7. Applying Ingress & External Routing..."
kubectl apply -f "${SCRIPT_DIR}/13-ingress.yaml"

echo "================================================================="
echo "✅ All manifests applied to namespace: petstore"
echo "Check pods with: kubectl get pods -n petstore"
echo "================================================================="
