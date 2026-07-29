# Shop module — UAT checklist

**Branch:** `feature/shop-v1`  
**Tester:** _______________  
**Environment:** _______________  
**Date signed off:** _______________

Mark each item **Pass / Fail / N/A**.

---

## 0. Prerequisites

| # | Check | Pass | Notes |
|---|--------|------|-------|
| 0.1 | Backend running with `modules.shop.enabled=true` | ☐ | |
| 0.2 | Frontend with `VITE_SHOP_MODULE_ENABLED=true` | ☐ | |
| 0.3 | SQL run: `add-shop-module.sql` through `add-shop-payments.sql` | ☐ | |
| 0.4 | Shop Portal shows **Phase S6** | ☐ | |

---

## 1. Storefront (customer)

| # | Test | Expected | Pass |
|---|------|----------|------|
| 1.1 | Browse `/store` as guest | Catalog loads with images | ☐ |
| 1.2 | Design detail — size/color picker | Stock and price update | ☐ |
| 1.3 | Add to cart (guest) | Cart badge updates; localStorage persists | ☐ |
| 1.4 | Login as **Customer** | Header shows **My account**, not login | ☐ |
| 1.5 | Guest cart merges on login | Server cart has guest items | ☐ |
| 1.6 | Checkout — select COD | Order placed, status pending | ☐ |
| 1.7 | Checkout — bank transfer + reference | Order placed, payment status pending | ☐ |
| 1.8 | **My Shop Orders** | Order visible; cancel pending works | ☐ |

---

## 2. Shop Portal (admin/manager)

| # | Test | Expected | Pass |
|---|------|----------|------|
| 2.1 | Shop Settings save | Store name/tagline update on `/store` | ☐ |
| 2.2 | Orders queue — confirm order | Status confirmed; stock reserved | ☐ |
| 2.3 | Record partial payment | payment_status partial; balance due correct | ☐ |
| 2.4 | Record full payment | payment_status paid | ☐ |
| 2.5 | Convert to bill | Bill created; no double stock deduction | ☐ |
| 2.6 | Cancel confirmed order | Stock restored | ☐ |
| 2.7 | Analytics page | Totals and top designs match orders | ☐ |

---

## 3. Finance integration (optional)

| # | Test | Expected | Pass |
|---|------|----------|------|
| 3.1 | Finance module enabled | — | ☐ |
| 3.2 | Payment on order with linked bill | AR journal posts (if auto-post on) | ☐ |

---

## 4. Email (optional)

| # | Test | Expected | Pass |
|---|------|----------|------|
| 4.1 | SMTP configured | Customer receives order placed email | ☐ |
| 4.2 | Confirm order | Confirmation email sent | ☐ |

---

## Issues log

| # | Issue | Severity | Status |
|---|--------|----------|--------|
| | | | |
