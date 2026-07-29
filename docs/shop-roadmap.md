# Shop module — roadmap

**Branch:** `feature/shop-v1` (based on `feature/finance-v2`)  
**Status:** S4 in progress  
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

### S1 — Foundation ✅

- [x] Branch `feature/shop-v1`, module flag wiring
- [x] SQL: `shop_settings` (store name, tagline, storefront toggle)
- [x] Shop Portal shell (`/shop`) — overview, settings, catalog preview
- [x] Public catalog API (`GET /api/shop/catalog`) — active designs with variants & stock
- [x] Public storefront route (`/store`) — browse without operations login

**Deliverable:** Admin can configure shop settings and preview what customers will see.

---

### S2 — Product detail & discovery ✅

- [x] Design detail page (`/store/design/:id`) with size/color picker and live stock
- [x] Category and featured filters on storefront
- [x] Search by design name/code (`q` param)
- [x] Guest browse vs login-to-order messaging

**Deliverable:** Customer can browse and inspect a design before adding to cart.

---

### S3 — Shopping cart ✅

- [x] Cart entity (logged-in customer) + guest cart in localStorage
- [x] Add/update/remove lines with stock validation
- [x] Cart summary (subtotal, customer discount if applicable)
- [x] Merge guest cart on customer login; persist server cart across devices
- [x] Cart page at `/store/cart` with header badge

**Deliverable:** Customer builds a cart with real-time stock checks.

---

### S4 — Checkout & shop orders (current)

- [x] Checkout form at `/store/checkout` (shipping/contact notes)
- [x] Create **shop order** linked to customer account
- [x] Order statuses: `pending`, `confirmed`, `fulfilled`, `cancelled`
- [x] Admin queue in Shop Portal (`/shop/orders`) — review and confirm orders
- [x] Convert confirmed shop order → **Quotation** or **Bill** in operations (bridge)

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
SOURCE backend/src/main/resources/db/add-shop-cart.sql;
SOURCE backend/src/main/resources/db/add-shop-orders.sql;
```

Shop Portal: http://localhost:3000/shop  
Public storefront: http://localhost:3000/store
