# Module architecture — Dreams Creations

## Two separate products

| Product | Repo / folder | Purpose |
|---------|---------------|---------|
| **Production ERP** | `dreams-creations` (this repo) | Factory batches, dispatch, inventory, sales, finance |
| **Online Shop** | `Projects/dreams-creations-shop` | E-commerce storefront, cart, checkout (separate app) |

The production ERP has **no shop UI, routes, or module options**. Shop backend/frontend live entirely in the shop project.

Finance in the production ERP can still receive **shop sales** via a backend integration API (not a shop module inside this app).

---

## Production ERP (`/modules`)

After login, Admin/Manager choose:

1. **Production & Operations** → `/dashboard`
2. **Finance Portal** → `/finance` (when finance module enabled)

There is **no shop tile** in this app.

---

## Shop → Finance integration (backend only)

The separate shop app posts paid orders to the production ERP:

```
POST http://localhost:8080/api/integration/shop/orders/sync
Header: X-Shop-Integration-Key: <app.shop.integration.api-key>
```

Creates bill `SHOP-{orderNumber}` and runs finance auto-posting (AR, COGS when enabled).

---

## Configuration

**Production ERP** (`application.properties`):

```properties
modules.finance.enabled=true
modules.finance.auto-post-ar=true
app.shop.integration.api-key=change-me-shop-integration-key
```

**Shop project** (separate):

```properties
server.port=8082
app.erp.url=http://localhost:8080
app.erp.integration.api-key=change-me-shop-integration-key
```

Bootstrap shop project:

```powershell
.\scripts\bootstrap-shop-project.ps1
```

---

## Dev ports

| App | Frontend | Backend |
|-----|----------|---------|
| Production ERP | 3000 | 8080 |
| Online Shop | 3002 | 8082 |
