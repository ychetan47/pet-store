# 🐾 Paws & Claws — Pet Store E-Commerce Platform

A production-grade, full-stack e-commerce platform for pet food, accessories, toys, and grooming products (supporting **Dogs** and **Cats**). Built with a **Spring Boot 3 (Java 17)** modular monolith backend and a modern, high-performance **React (TypeScript + Vite)** frontend, fully containerized with **Docker & Docker Compose**.

---

## 📑 Table of Contents

- [Overview & Key Features](#-overview--key-features)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Repository Structure](#-repository-structure)
- [Quick Start: Running with Docker (Recommended)](#-quick-start-running-with-docker-recommended)
- [Local Bare-Metal Setup](#-local-bare-metal-setup)
- [Database Schema & Migrations](#-database-schema--migrations)
- [Environment Variables & Configuration](#-environment-variables--configuration)
- [Interactive API Docs (Swagger / OpenAPI 3)](#-interactive-api-docs-swagger--openapi-3)
- [REST API Reference & cURL Examples](#-rest-api-reference--curl-examples)
- [Running Automated Tests](#-running-automated-tests)
- [Production & Container Details](#-production--container-details)

---

## 🌟 Overview & Key Features

### Customer Shopping Experience
- **Hierarchical Category Tree**: Seamless multi-level catalog navigation for Dogs & Cats with subcategories (Food, Accessories, Toys, Grooming, Dry/Wet Food, etc.).
- **Dynamic Product Filtering & Search**: Instant filtering by pet type, category subtree, brand, price range, stock availability, and sorting (price low-to-high, high-to-low, newest).
- **Persistent Cart & Stock Sync**: Authenticated shopping cart with instant quantity updates and real-time inventory validation.
- **Address Book Management**: Multi-address support with default shipping address selection.
- **Transactional COD Checkout**: Cash-on-delivery ordering with pessimistic inventory reservation to guarantee zero overselling.
- **Order Tracking & Eligible Cancellation**: Order lifecycle history with the ability to cancel eligible orders (`PLACED` or `CONFIRMED`), automatically restoring product stock.

### Enterprise Architecture Highlights
- **Concurrency & Inventory Safety**: Checkout operations utilize **JPA Pessimistic Write Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on the product stock to eliminate race conditions and double-selling under concurrent checkouts.
- **Historical Order Snapshots**: Order line items store point-in-time product names, images, and prices, insulating past transactions from catalog modifications.
- **Clean Storage Abstraction**: Pluggable `StorageService` interface ready for Google Cloud Storage (GCS) and local asset fallbacks.
- **Containerized Reverse Proxy**: Nginx reverse proxies API traffic and Swagger docs internally to backend services, avoiding cross-origin overhead and providing SPA routing.

---

## 🏛️ System Architecture

```mermaid
graph TD
    Client["Browser / Client (Desktop & Mobile)"]
    
    subgraph Docker Network: petstore-network
        Nginx["Nginx Reverse Proxy & Static Asset Server (Port 5173 / 80)"]
        SPA["React + TypeScript SPA (Vite Build)"]
        Backend["Spring Boot 3 API Server (Java 17 JRE) (Port 8080)"]
        DB[(PostgreSQL 16 Database) (Port 5432)]
    end

    Client -->|HTTP :5173| Nginx
    Nginx -->|Serves Static Files| SPA
    Nginx -->|Proxies /api & /swagger-ui| Backend
    Backend -->|JDBC / HikariCP| DB
    Backend -->|Storage Abstraction| GCS["Cloud Storage / Local Media"]
```

---

## 🛠️ Tech Stack

### Frontend
- **Framework**: React 18 / 19 with TypeScript
- **Build Tool**: Vite 6 (Fast HMR & optimized production bundling)
- **Routing**: React Router 6 (SPA with history API fallback)
- **HTTP Client**: Axios (configured with JWT auth interceptor and 401 handling)
- **Design System**: Vanilla CSS tokens & utilities (Responsive CSS Grid/Flexbox, glassmorphism, Google Fonts Outfit & Plus Jakarta Sans)

### Backend
- **Language & Runtime**: Java 17 (Eclipse Temurin)
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security with Stateless JWT (`io.jsonwebtoken:jjwt 0.12.6`)
- **Data & Persistence**: Spring Data JPA, Hibernate, HikariCP
- **Database Migrations**: Flyway 10
- **API Documentation**: Springdoc OpenAPI 3 / Swagger UI 2.6.0
- **Validation**: Jakarta Bean Validation
- **Testing**: JUnit 5, Mockito, Spring Security Test, H2 In-Memory Database (21 tests)

### Database & Infrastructure
- **Database**: PostgreSQL 16 Alpine
- **Containerization**: Multi-stage Dockerfiles (`node:20-alpine` + `nginx:alpine` for frontend, `maven:3.9-eclipse-temurin-17` + `eclipse-temurin:17-jre-alpine` for backend)
- **Orchestration**: Docker Compose v2

---

## 📁 Repository Structure

```text
pet-store/
├── backend/
│   ├── src/main/java/com/petstore/
│   │   ├── common/           # Global exception handler, ApiResponse envelope, CORS, Security config
│   │   ├── auth/             # Customer registration, login, JWT token provider & authentication filter
│   │   ├── category/         # Hierarchical category tree entity, repository & descendant resolution
│   │   ├── product/          # Product catalog, dynamic JPA Specification filtering, images
│   │   ├── inventory/        # Concurrency-safe pessimistic write lock inventory service
│   │   ├── cart/             # Shopping cart entities, DTOs & stock validation service
│   │   ├── address/          # Customer delivery address management (CRUD, default switching)
│   │   ├── order/            # Transactional COD checkout, order history, cancellation & restock
│   │   └── storage/          # GCS & Local image storage abstraction
│   ├── src/main/resources/
│   │   ├── application.yml   # PostgreSQL, Flyway, JWT & CORS configuration
│   │   └── db/migration/     # Flyway SQL migrations
│   │       ├── V1__create_tables.sql
│   │       ├── V2__insert_categories.sql
│   │       └── V3__insert_products.sql
│   ├── src/test/             # 21 comprehensive JUnit 5 & Mockito test suites
│   ├── .dockerignore
│   ├── Dockerfile            # Multi-stage Maven build + lightweight JRE 17 runtime
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── assets/           # Icons, brand marks, and SVG assets
│   │   ├── components/       # Navbar, Footer, ProductCard, CategoryFilter, Toast, Modals
│   │   ├── context/          # AuthContext, CartContext, ToastContext
│   │   ├── pages/            # Home, Catalog, ProductDetail, Cart, Checkout, Orders, Addresses, Profile, Auth
│   │   ├── services/         # Axios API clients (auth, products, categories, cart, orders, addresses)
│   │   ├── types/            # TypeScript interfaces & API payload types
│   │   ├── App.tsx           # Route definitions & layout wrappers
│   │   └── index.css         # Global CSS variables, typography, component utilities
│   ├── nginx.conf            # Nginx reverse proxy (/api -> backend:8080) & SPA static file server
│   ├── .dockerignore
│   ├── Dockerfile            # Multi-stage Vite build + Nginx Alpine server
│   ├── index.html
│   ├── vite.config.ts
│   └── package.json
│
├── docker-compose.yml        # Multi-container orchestration (PostgreSQL 16 + Backend + Frontend)
└── README.md
```

---

## 🚀 Quick Start: Running with Docker (Recommended)

The easiest way to run the entire application (Database, Spring Boot backend, and React frontend) is with Docker Compose.

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

### 1. Launch All Services
Run the following command from the root of the project:

```bash
docker compose up --build -d
```

Docker Compose will automatically:
1. Start PostgreSQL 16 on port `5432` with a persistent volume (`postgres_data`).
2. Wait for PostgreSQL health check (`pg_isready`) to pass.
3. Build and launch the Spring Boot backend on port `8080`, applying Flyway migrations `V1`, `V2`, and `V3` on boot.
4. Build the React frontend into static assets and serve it with Nginx on port `5173`.

### 2. Access the Applications

| Service | Host URL | Description |
| :--- | :--- | :--- |
| **Frontend Web App** | [http://localhost:5173](http://localhost:5173) | Customer storefront, catalog, cart & checkout |
| **Backend REST API** | [http://localhost:8080](http://localhost:8080) | Spring Boot REST API endpoints |
| **Swagger / OpenAPI UI** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | Interactive API documentation & testing |
| **PostgreSQL Database** | `localhost:5432` | Database: `petstoredb`, User: `postgres`, Password: `postgrespassword` |

### 3. Helpful Docker Commands

```bash
# View real-time logs for all services
docker compose logs -f

# View logs for a specific service
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres

# Stop all containers
docker compose down

# Stop and remove all volumes (resets database)
docker compose down -v

# Connect directly to the PostgreSQL container shell
docker exec -it petstore-postgres psql -U postgres -d petstoredb
```

---

## 💻 Local Bare-Metal Setup

If you prefer to run services directly on your host machine for development:

### Prerequisites
- **Java**: 17+
- **Maven**: 3.8+
- **Node.js**: 18+ and `npm`
- **PostgreSQL**: 16+ running locally on port `5432`

### 1. Configure the Database
Create the database in PostgreSQL:
```bash
createdb petstoredb
```
*(Default credentials expected: username `postgres`, password `postgrespassword` or configure via environment variables).*

### 2. Run the Backend
```bash
cd backend
mvn spring-boot:run
```
Flyway will automatically create tables and seed categories and products. The backend will start at `http://localhost:8080`.

### 3. Run the Frontend
In a separate terminal window:
```bash
cd frontend
npm install
npm run dev
```
The Vite development server will start at `http://localhost:5173` with fast hot-module reloading.

---

## 🗄️ Database Schema & Migrations

Database versioning is managed via **Flyway** in [backend/src/main/resources/db/migration/](file:///Users/chetan/projects/pet-store/backend/src/main/resources/db/migration/):

1. **`V1__create_tables.sql`**:
   - `users`: Customer accounts with hashed passwords and unique email index.
   - `categories`: Self-referential hierarchical tree (`parent_id REFERENCES categories(id)`).
   - `products`: Product catalog with check constraints (`stock_quantity >= 0`, `price >= 0`) and category foreign keys.
   - `product_images`: Multi-image support with `is_primary` and `display_order`.
   - `addresses`: Customer shipping addresses with default address flag.
   - `carts` & `cart_items`: Unique cart per user, cascade deletion, and quantity checks.
   - `orders` & `order_items`: Order records, status tracking (`PLACED`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`), snapshot pricing, and delivery address snapshot.
2. **`V2__insert_categories.sql`**:
   - Seeds root categories (**Dogs** and **Cats**) and nested subcategories:
     - Dog Food (Dry Dog Food, Wet Dog Food, Puppy Food, Dog Treats)
     - Dog Accessories (Collars, Leashes, Harnesses, Beds, Bowls)
     - Dog Toys & Dog Grooming
     - Cat Food (Dry Cat Food, Wet Cat Food, Kitten Food, Cat Treats)
     - Cat Accessories (Litter Boxes, Bowls, Beds, Carriers)
     - Cat Toys & Cat Grooming
3. **`V3__insert_products.sql`**:
   - Seeds realistic pet products from top brands (*Royal Canin, Pedigree, Whiskas, Arden Grange, Furminator, KONG, Drools*) with high-resolution image URLs, descriptions, prices, and initial stock quantities.

---

## ⚙️ Environment Variables & Configuration

The application is configured via `backend/src/main/resources/application.yml` and can be customized with the following environment variables:

| Environment Variable | Default Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | Backend HTTP server port |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/petstoredb` | JDBC connection URL (set to `jdbc:postgresql://postgres:5432/petstoredb` in Docker) |
| `DATABASE_USERNAME` | `postgres` (in Docker) | PostgreSQL database username |
| `DATABASE_PASSWORD` | `postgrespassword` | PostgreSQL database password |
| `JWT_SECRET` | Base64-encoded 512-bit test secret | Secret key used for signing HS512 JWT tokens |
| `JWT_EXPIRATION_MS` | `86400000` (24 hours) | JWT token lifespan in milliseconds |
| `STORAGE_PROVIDER` | `gcs` | Storage provider implementation (`gcs` or `local`) |
| `GCS_BUCKET` | `pet-store-bucket` | Target Google Cloud Storage bucket name |
| `STORAGE_BASE_URL` | `https://storage.googleapis.com/pet-store-bucket` | Public URL prefix for image assets |
| `CORS_ALLOWED_ORIGINS`| `http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173` | Allowed origins for cross-origin requests |

---

## 📖 Interactive API Docs (Swagger / OpenAPI 3)

The backend provides interactive OpenAPI documentation via **Springdoc OpenAPI**:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI 3 JSON Specification**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Authorizing in Swagger UI
1. Execute `POST /api/auth/register` or `POST /api/auth/login` to obtain a JWT token.
2. Click the green **Authorize 🔓** button at the top-right of the Swagger page.
3. Paste the token into the value field and click **Authorize**.
4. All secured endpoints (`/api/cart/**`, `/api/addresses/**`, `/api/orders/**`) will automatically include your `Bearer <token>` header.

---

## 📋 REST API Reference & cURL Examples

### 1. Authentication
- `POST /api/auth/register` — Register a new customer
- `POST /api/auth/login` — Authenticate and receive a JWT token
- `GET /api/auth/me` — Current user profile (`Bearer <token>` required)

**Example Registration:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "Password123!",
    "phone": "9876543210"
  }'
```

---

### 2. Categories
- `GET /api/categories` — Flat list of active categories
- `GET /api/categories/tree` — Hierarchical category tree (Dogs & Cats hierarchies)
- `GET /api/categories/{id}` — Category details by ID

**Example Fetch Category Tree:**
```bash
curl -X GET http://localhost:8080/api/categories/tree
```

---

### 3. Products
- `GET /api/products` — Filter products by:
  - `pet` (e.g. `dogs`, `cats`)
  - `categoryId` (includes descendants automatically)
  - `brand`
  - `minPrice` & `maxPrice`
  - `search` (keyword search on name, description, brand)
  - `page`, `size`, `sort`
- `GET /api/products/{id}` — Product details with image gallery and category breadcrumb
- `GET /api/products/brands` — Distinct brands in catalog

**Example Filter Products:**
```bash
curl -X GET "http://localhost:8080/api/products?pet=dogs&minPrice=500&maxPrice=3000&sort=price,asc"
```

---

### 4. Shopping Cart *(Requires `Authorization: Bearer <token>`)*
- `GET /api/cart` — View current customer's cart
- `POST /api/cart/items` — Add item to cart (`{ "productId": 101, "quantity": 1 }`)
- `PUT /api/cart/items/{id}` — Update item quantity (`{ "quantity": 3 }`)
- `DELETE /api/cart/items/{id}` — Remove item from cart
- `DELETE /api/cart` — Clear entire cart

**Example Add to Cart:**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"productId": 101, "quantity": 2}'
```

---

### 5. Delivery Addresses *(Requires `Authorization: Bearer <token>`)*
- `GET /api/addresses` — List saved delivery addresses
- `POST /api/addresses` — Save new address
- `PUT /api/addresses/{id}` — Update address
- `DELETE /api/addresses/{id}` — Delete address

**Example Add Address:**
```bash
curl -X POST http://localhost:8080/api/addresses \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "phone": "9876543210",
    "addressLine1": "Flat 402, Green Valley Apartments",
    "addressLine2": "Indiranagar",
    "city": "Bengaluru",
    "state": "Karnataka",
    "pincode": "560038",
    "isDefault": true
  }'
```

---

### 6. Orders & Checkout *(Requires `Authorization: Bearer <token>`)*
- `POST /api/orders` — Place Cash on Delivery order from cart (`{ "addressId": 1, "paymentMethod": "COD" }`)
- `GET /api/orders` — View customer's order history
- `GET /api/orders/{id}` — Detailed order view with line items and status tracking
- `POST /api/orders/{id}/cancel` — Cancel eligible order (`PLACED` or `CONFIRMED`), restoring inventory

**Example Place Order:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"addressId": 1, "paymentMethod": "COD"}'
```

---

## 🧪 Running Automated Tests

The backend includes a comprehensive suite of **21 unit and integration tests** covering security, token generation, category tree resolution, dynamic product queries, cart operations, pessimistic write lock checkout concurrency, and order cancellation stock recovery.

Run the test suite via Maven:

```bash
cd backend
mvn test
```

### Key Test Suites
- `CartServiceTest`: Cart validation, adding items, quantity updates, removing items, and clearing cart.
- `OrderServiceTest`: Concurrency-safe checkout, zero overselling, order placement, order history, and cancellation.
- `InventoryServiceTest`: Pessimistic lock verification and stock deduction/restoration.
- `ProductServiceTest` & `CategoryServiceTest`: Hierarchy resolution and catalog filtering.
- `AuthServiceTest` & `JwtTokenProviderTest`: Customer registration, password hashing, and token signing/validation.

---

## 📦 Production & Container Details

### Frontend Nginx Container (`frontend/Dockerfile` & `frontend/nginx.conf`)
- **Stage 1 (Build)**: Compiles TypeScript and builds React via `node:20-alpine` with `npm ci`.
- **Stage 2 (Runtime)**: Runs lightweight `nginx:alpine` serving the static build.
- **Reverse Proxy**: Internal proxy rules route `/api/*` and `/swagger-ui/*` directly to `http://backend:8080`, eliminating browser CORS issues in container environments.
- **SPA Routing**: `try_files $uri $uri/ /index.html` ensures all client-side routes (e.g. `/cart`, `/checkout`, `/orders/12`) resolve smoothly without 404 errors on browser refresh.

### Backend Container (`backend/Dockerfile`)
- **Stage 1 (Build)**: Packages Spring Boot fat JAR with `maven:3.9-eclipse-temurin-17`.
- **Stage 2 (Runtime)**: Executes minimal JRE on `eclipse-temurin:17-jre-alpine` running as an unprivileged user for security.
