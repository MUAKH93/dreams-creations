# Shop module — roadmap

**Branch:** `feature/shop-v1` (based on `feature/finance-v2`)  
**Status:** S1 in progress  
**Go-live:** After operations module is live; finance merge is deferred until then.

The shop module adds a **public online storefront** and a **Shop Portal** for Admin/Manager — separate from the internal operations ERP, following the same pattern as Finance (`modules.shop.enabled`).

---

## Architecture

| Layer | Pattern |
|-------|---------|
| Backend | `controller/shop`, `service/shop`, `entity/shop`, `@ConditionalOnProperty("modules.shop.enabled=true")` |
| SQL | Manual scripts in `backend/src/main/resources/db/add-shop-*.sql` |
| Frontend | `ShopLayout`, routes under `/shop`, public storefront at `/store` |
| Reuse | **Design → Suit → Product → Inventory** for catalog; **Customer → Quotation/Bill** for orders (later phases) |

---

## Phases

### S1 — Foundation (current)

- [x] Branch `feature/shop-v1`, module flag wiring
- [x] SQL: `shop_settings` (store name, tagline, storefront toggle)
- [x] Shop Portal shell (`/shop`) — overview, settings, catalog preview
- [x] Public catalog API (`GET /api/shop/catalog`) — active designs with variants & stock
- [x] Public storefront route (`/store`) — browse without operations login

**Deliverable:** Admin can configure shop settings and preview what customers will see.

---

### S2 — Product detail & discovery

- Design detail page with size/color picker and live stock
- Category and featured filters on storefront
- Search by design name/code
- “Login to order” vs guest browse messaging

**Deliverable:** Customer can browse and inspect a design before adding to cart.

---

### S3 — Shopping cart

- Cart entity (logged-in customer) + session/guest cart strategy
- Add/update/remove lines with stock validation
- Cart summary (subtotal, customer discount if applicable)
- Persist cart across devices for registered customers

**Deliverable:** Customer builds a cart with real-time stock checks.

---

### S4 — Checkout & shop orders

- Checkout form (shipping/contact notes)
- Create **shop order** linked to customer account
- Order statuses: `pending`, `confirmed`, `fulfilled`, `cancelled`
- Admin queue in Shop Portal — review and confirm orders
- Convert confirmed shop order → **Quotation** or **Bill** in operations (bridge)

**Deliverable:** End-to-end order placement without staff manually creating bills.

---

### S5 — Fulfillment & operations integration

- Stock reservation on order confirm
- Deduct inventory when order fulfilled (reuse bill stock logic)
- Production trigger for made-to-order lines (optional flag per design)
- Customer “My shop orders” page (alongside existing quotes/bills)
- Email notifications (order placed, confirmed, shipped)

**Deliverable:** Shop orders flow into factory inventory and production.

---

### S6 — Payments & polish

- Payment methods: COD, bank transfer, optional gateway stub
- Partial/full payment recording (reuse `PaymentService`)
- Finance AR auto-post when finance module enabled
- Shop analytics (orders, revenue, top designs)
- UAT checklist + tutorial content

**Deliverable:** Production-ready shop with sign-off checklist.

---

## Deployment notes

| When | Action |
|------|--------|
| Dev | `modules.shop.enabled=true`, `VITE_SHOP_MODULE_ENABLED=true`, run `add-shop-module.sql` |
| Staging | Enable shop after operations UAT; run shop SQL |
| Production | Enable with operations go-live; finance can remain off until finance merge |

---

## Dependencies

| Module | Relationship |
|--------|--------------|
| **Operations** | Catalog data (designs, suits, products, inventory) — **required** |
| **Finance** | Optional AR auto-post on shop payments (when enabled) |
| **Customer portal** | Registered customers reuse same login; shop extends `/my-orders` |

---

## Out of scope (later)

- Multi-currency
- International shipping zones
- Marketplace / multi-vendor
- Mobile app (PWA may be added in S6)

---

## Quick start (developers)

```powershell
git checkout feature/shop-v1
# Backend application.properties
modules.shop.enabled=true
# Frontend .env
VITE_SHOP_MODULE_ENABLED=true
# MySQL
SOURCE backend/src/main/resources/db/add-shop-module.sql;
```

Shop Portal: http://localhost:3000/shop  
Public storefront: http://localhost:3000/store
