# 🐾 Pet Store Frontend — React + TypeScript + Vite

Modern, high-performance customer-facing storefront for the **Paws & Claws Pet Store Platform**.

---

## 🛠️ Tech Stack & Libraries

- **Framework**: React 18 / 19 with TypeScript
- **Bundler & Dev Server**: [Vite](https://vitejs.dev/)
- **Routing**: [React Router 6](https://reactrouter.com/) (Declarative client-side routing)
- **HTTP Client**: [Axios](https://axios-http.com/) (configured with JWT interceptors & error handlers)
- **Design System**: Vanilla CSS tokens & utilities (Glassmorphism, responsive grid/flexbox, Google Fonts Outfit & Plus Jakarta Sans)

---

## 📁 Source Directory Layout

```text
frontend/src/
├── assets/           # Icons, logo marks, graphics
├── components/       # Reusable UI elements (Navbar, Footer, ProductCard, CategoryFilter, Toast, Modal)
├── context/          # React Context Providers (AuthContext, CartContext, ToastContext)
├── pages/            # View pages
│   ├── HomePage.tsx            # Hero, category pills, featured deals, benefits
│   ├── ProductListingPage.tsx  # Dynamic multi-facet filters & catalog grid
│   ├── ProductDetailPage.tsx   # Image gallery, stock badge, quantity picker, reviews
│   ├── CartPage.tsx            # Cart item table, quantity stepper, subtotal summary
│   ├── CheckoutPage.tsx        # Multi-step checkout with address selection & COD
│   ├── OrdersPage.tsx          # Past orders list with status indicators
│   ├── OrderDetailPage.tsx     # Itemized order details & eligible cancellation
│   ├── AddressesPage.tsx       # Address book management
│   ├── ProfilePage.tsx         # Account summary
│   ├── LoginPage.tsx           # Customer sign-in
│   └── RegisterPage.tsx        # Customer registration
├── services/         # Axios API clients (auth, products, categories, cart, orders, addresses)
├── types/            # TypeScript interfaces & API response contracts
├── App.tsx           # Route layout & context providers wrapping
└── index.css         # Design system tokens, typography, CSS resets
```

---

## 🚀 Running Locally (Standalone)

### 1. Install Dependencies
```bash
npm install
```

### 2. Start Vite Dev Server
```bash
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.

> **Note**: For local standalone dev, requests to `/api` proxy through Vite's dev server or Axios `baseURL` pointing to `http://localhost:8080`.

### 3. Build for Production
```bash
npm run build
```
Generates production-optimized static assets in `dist/`.

---

## 🐳 Docker Deployment

The frontend includes a multi-stage [Dockerfile](file:///Users/chetan/projects/pet-store/frontend/Dockerfile) and [nginx.conf](file:///Users/chetan/projects/pet-store/frontend/nginx.conf):
- **Port 5173:80** in Docker Compose.
- **Reverse Proxy**: Proxies `/api` and `/swagger-ui` to `http://backend:8080` internally.
- **SPA Fallback**: Configured with `try_files $uri $uri/ /index.html` to eliminate 404s on browser reloads.
