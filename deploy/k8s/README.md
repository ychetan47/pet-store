# Kubernetes / GKE Deployment Manifests (Strategy B)

This directory contains the Kubernetes manifests for deploying the **Paws & Claws Pet Store** microservices application to **Google Kubernetes Engine (GKE)**.

## Architecture
- **Ingress**: GKE Ingress / Nginx Ingress Controller routing public traffic
- **Deployments & Services**:
  - `api-gateway`
  - `customer-frontend`
  - `admin-frontend`
  - `user-service`
  - `catalog-service`
  - `inventory-service`
  - `order-service`
  - `notification-service`
- **Configuration & Secrets**:
  - `configmap.yaml`
  - `secret.yaml`
- **Event Bus & Database**:
  - In-cluster Kafka / Managed Kafka
  - Cloud SQL Auth Proxy / PostgreSQL StatefulSet
