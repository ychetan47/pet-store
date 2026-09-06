# 🐾 Pet Store Backend — Spring Boot 3 (Java 17)

Modular monolith REST API backend for the **Paws & Claws Pet Store Platform**.

---

## 🛠️ Tech Stack & Key Libraries

- **Language & JDK**: Java 17 (Eclipse Temurin)
- **Framework**: Spring Boot 3.3.4
- **Persistence**: Spring Data JPA, Hibernate ORM, HikariCP
- **Database Migrations**: Flyway 10
- **Security**: Spring Security 6 with stateless JWT authentication (`io.jsonwebtoken:jjwt 0.12.6`)
- **API Documentation**: Springdoc OpenAPI 3 (`2.6.0`) & Swagger UI
- **Testing**: JUnit 5, Mockito, Spring Security Test, H2 In-Memory DB

---

## 📁 Package Architecture (Modular Monolith)

Organized into clean domain packages ready for future microservices extraction:

```text
backend/src/main/java/com/petstore/
├── common/           # Global exception handler, ApiResponse<T> envelope, CORS, SecurityFilterChain
├── auth/             # Customer registration, login, JWT token provider & request filter
├── category/         # Hierarchical category tree entity, repository & descendant resolution
├── product/          # Product catalog, dynamic JPA Specification filtering, images
├── inventory/        # Concurrency-safe pessimistic write lock inventory reservation service
├── cart/             # Shopping cart entities, DTOs & stock validation service
├── address/          # Customer delivery address management (CRUD, default switching)
├── order/            # Transactional COD checkout, order history, cancellation & restock
└── storage/          # GCS & Local image storage abstraction
```

---

## 🚀 Running Locally (Standalone)

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 16 running on port 5432 with database `petstoredb`

### 1. Database Setup
```bash
createdb petstoredb
```

### 2. Run the Application
```bash
mvn spring-boot:run
```
Flyway automatically executes all pending migrations on startup:
- `V1__create_tables.sql`
- `V2__insert_categories.sql`
- `V3__insert_products.sql`

Backend will listen on [http://localhost:8080](http://localhost:8080).

---

## 🧪 Testing

Run all 21 unit and integration tests (uses in-memory H2 database):

```bash
mvn test
```

Test coverage includes:
- Pessimistic write locking & stock safety under concurrent checkout
- Cart stock validation & item lifecycle
- Order cancellation and inventory restoration
- JWT token generation and validation
- Dynamic product catalog queries and category hierarchy traversal

---

## 📖 Swagger / OpenAPI Documentation

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

To test secured endpoints, login or register to get a JWT token and click the **Authorize** button in Swagger UI.

---

## ⚙️ Key Environment Variables

| Variable | Default | Purpose |
| :--- | :--- | :--- |
| `PORT` | `8080` | Server HTTP port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/petstoredb` | PostgreSQL JDBC connection URL |
| `DATABASE_USERNAME` | `postgres` | Database username |
| `DATABASE_PASSWORD` | `postgrespassword` | Database password |
| `JWT_SECRET` | Base64-encoded test secret | Signing key for HS512 JWT |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | JWT lifespan in milliseconds |
| `CORS_ALLOWED_ORIGINS`| `http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173` | Allowed CORS origins |
| `STORAGE_PROVIDER` | `gcs` | `gcs` or `local` |
| `GCS_BUCKET` | `pet-store-bucket` | Google Cloud Storage bucket |
