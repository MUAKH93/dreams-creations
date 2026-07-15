# Dreams Creations — Factory Management System

**Client proposal · Operations Edition (v1.0)**  
**Prepared for:** *[Client / Factory name]*  
**Prepared by:** *[Your name / company]*  
**Date:** July 2026  
**Valid for:** 30 days

---

## 1. Executive summary

**Dreams Creations** is a custom web application for textile / suit manufacturing: production tracking from design through packing, finished-goods inventory, sales billing, and customer self-service. It replaces manual registers and scattered spreadsheets with one system accessible by **Admin**, **Manager**, **Supervisor**, and **Customer** roles.

This proposal covers:

- What is **delivered today** (Operations Edition)
- **Pricing** for deployment and ongoing support
- How **bugs and deployment issues** are handled after go-live
- How **future modules** (finance, online shop, payments) are scoped and priced

---

## 2. What is included today — Operations Edition (v1.0)

### Technology

| Layer | Stack |
|-------|--------|
| Backend | Java 17, Spring Boot, REST API, JWT security |
| Frontend | React, Vite, Ant Design |
| Database | MySQL |
| Deployment | Self-hosted VPS (e.g. Hetzner ~$6–10/month) or client server |

### User roles & access

| Role | Purpose |
|------|---------|
| **Admin** | Full access, staff accounts, factory setup, password reset for staff |
| **Manager** | Day-to-day operations — production, inventory, sales, reports |
| **Supervisor** | Own assignments, record returns, view designs |
| **Customer** | Browse designs, request quotes, view bills (self-registration + email verification) |

Single sign-in page for all users; dashboard and menu adapt by role.

### Production management

- Start production orders (single or **multiple designs** per order)
- Auto batch numbers; design label and article name on batches
- Configurable stage path per design: **Designing → (optional Filling) → Cutting & Stitching → Press and Packing**
- **Dispatch** to stages with supervisor, due dates, work types (designing / filling)
- Size/color breakdown at Cutting & Stitching
- **Returns** with OK / damaged / missing reconciliation
- Auto-forward from Cutting & Stitching to **Press and Packing**
- **Inventory updates only** when Press and Packing is returned
- Batch status (planned / in progress / completed) based on full accountability
- Dispatch Management with **per-stage tabs** for clearer view
- Supervisor **My Assignments** for recording returns

### Inventory

- Stock by design, size, and color
- Updates from production returns; deductions on bill confirmation
- Low-stock alerts (active designs only)
- Manual stock adjustments with audit trail

### Catalog

- Designs with images, categories, types, sizes, colors (suits/SKUs)
- Design **active / inactive** status
- Per-design production stage configuration (including optional Filling)

### Sales & customers

- Customer records with **running balance**
- **Quotations** (management) and customer quote requests
- **Bills** with line items, discount, previous balance, grand total
- **A4 bill print** layout
- Payments recorded against bills; customer payment history
- Customer portal: **My Quotes**, **My Bills**

### Administration & operations

- Factory Setup (stages, modules, categories, sizes, work types, **packing supervisor**)
- Staff & supervisor login account management
- Admin **password reset** for staff (not customers)
- Alerts (view, dismiss)
- Activity log
- Reports & analytics dashboards
- User profile and photo

### Security & account features

- Login, registration (customers), forgot / reset password
- Email verification for new customers
- Role-based API and page protection

### Not included in v1.0 (future modules — see Section 5)

- Full accounting (chart of accounts, balance sheet, P&L, bank reconciliation)
- Shopping cart and online checkout
- Online payment gateways (JazzCash, EasyPaisa, Stripe, etc.)
- Multi-branch / multi-factory
- Mobile native apps
- Auditor-certified financial statements

---

## 3. Investment — pricing (editable)

*Adjust amounts below before sending to the client.*

### Option A — One-time project (recommended for first factory)

| Item | Amount (PKR) | Amount (USD)* |
|------|--------------|---------------|
| **Operations Edition (v1.0)** — design, build, configure for your factory | **1,200,000** | **~4,300** |
| **Go-live package** — VPS deploy, SSL, backup setup, smoke testing, 2 training sessions | **150,000** | **~540** |
| **Warranty** — bug fixes on delivered features (90 days) | *Included* | *Included* |
| **Total (Year 1 setup)** | **1,350,000** | **~4,840** |

\*USD indicative at ~PKR 279/USD; quote can be fixed in either currency.

### Option B — Annual license + setup

| Item | Amount (PKR) |
|------|--------------|
| One-time setup & configuration | **400,000** |
| Annual license (software + updates on Operations Edition) | **250,000 / year** |
| Go-live & training | **150,000** (once) |

### Option C — Ongoing support (after warranty)

| Tier | Monthly (PKR) | Includes |
|------|---------------|----------|
| **Hosting care** | **25,000** | Server monitoring, backups, security patches, critical fixes |
| **Business support** | **60,000** | Hosting care + email support, minor tweaks, monthly health check |
| **Priority** | **120,000** | Business support + priority response, small feature requests |

*Hosting server cost (VPS ~PKR 1,500–3,000/month) can be paid directly by client or bundled.*

### Payment schedule (Option A)

| Milestone | % | Amount (PKR) |
|-----------|---|--------------|
| Proposal acceptance & project start | 40% | 540,000 |
| UAT sign-off (staging tested by client) | 40% | 540,000 |
| Production go-live + handover | 20% | 270,000 |

---

## 4. Go-live, support & how issues are resolved

### Deployment procedure

1. Provision VPS (or use client server) and MySQL  
2. Deploy backend API + React frontend + HTTPS  
3. Run database migrations and factory-specific configuration  
4. Smoke test: login, batch, dispatch, return, bill, inventory  
5. Training: Admin/Manager (2 hrs), Supervisors (1 hr) — optional customer walkthrough  
6. Switch to production URL  

### Warranty (90 days from go-live)

**Included at no extra charge:**

- Defects in features listed in Section 2 (system errors, wrong calculations, data not saving)
- Deployment issues caused by delivered software or standard deployment steps

**Not included in warranty:**

- New features or process changes (“we now need X”)
- Issues caused by client server changes without notice
- Third-party outages (email SMTP, payment providers — when added later)

### Issue handling process

| Step | Action |
|------|--------|
| 1 | Client reports issue (email / WhatsApp / ticket) with screenshot and steps |
| 2 | Severity assigned: **Critical** (down) / **High** (wrong data) / **Medium** / **Low** |
| 3 | Reproduce on staging or review logs |
| 4 | Fix → test → deploy; database backup before production changes |
| 5 | Confirm with client; note in release log |

### Response targets (with paid support — Option C)

| Severity | First response | Target resolution |
|----------|----------------|-------------------|
| Critical | 4 business hours | Same business day |
| High | 1 business day | 2–3 business days |
| Medium / Low | 2–3 business days | Next planned update |

### Upgrades after deployment

New work is delivered as **versioned modules**, not open-ended changes:

1. **Discovery** — short meeting + written scope (in / out)  
2. **Fixed quote** — client approves price and timeline  
3. **Build on staging** — client tests without affecting live factory  
4. **UAT sign-off** — then production deploy with migration + rollback plan  
5. **Module warranty** — 90 days on that module only  

---

## 5. Future modules — roadmap & indicative pricing

*Final price after a requirements workshop; ranges below are for planning.*

| Module | Version | Scope (summary) | Timeline | Indicative price (PKR) |
|--------|---------|-----------------|----------|-------------------------|
| **Financial accounting** | v2.0 | Chart of accounts, journals, trial balance, balance sheet, P&L, cash flow | 10–14 weeks | **700,000 – 1,200,000** |
| **Customer online shop** | v2.1 | Browse catalog, cart, checkout UI, order status | 6–10 weeks | **400,000 – 800,000** |
| **Online payments** | v2.2 | Gateway integration (JazzCash / EasyPaisa / card), webhooks, reconciliation | 4–8 weeks | **300,000 – 600,000** |
| **Advanced reporting** | v2.3 | Custom exports, scheduled reports, management dashboards | 4–6 weeks | **250,000 – 500,000** |
| **Multi-branch** | v3.0 | Separate locations, stock transfers, branch-wise P&L | 8–12 weeks | **500,000 – 1,000,000** |

**Bundle example:** Finance + Shop + Payments — **10–15% discount** if contracted together.

Each module quote will include: deliverables list, exclusions, payment milestones (40% / 40% / 20%), and warranty terms.

---

## 6. What the client provides

- Timely feedback during UAT (typically 1–2 weeks)
- Production-stage names and factory workflow confirmation
- Server access or approval for VPS provisioning
- SMTP details for customer emails (or approval of provider)
- Named contacts: Admin, IT/hosting, and business owner for sign-off

---

## 7. Assumptions & exclusions

- Single factory / single legal entity in v1.0  
- Internet access required for web application  
- Financial module outputs are management reports, not substitute for statutory audit unless separately agreed  
- Payment gateways require client’s business verification with the provider  
- Content (logo, design images) supplied by client  

---

## 8. Next steps

| # | Action | Owner |
|---|--------|--------|
| 1 | Review this proposal; adjust pricing if needed | *[Developer]* |
| 2 | Client meeting — confirm scope and open questions | Both |
| 3 | Signed acceptance or PO + first milestone payment | Client |
| 4 | Staging URL for UAT | *[Developer]* |
| 5 | Go-live date agreed | Both |

---

## 9. Acceptance

| | |
|---|---|
| **Client name** | _________________________________ |
| **Authorized signature** | _________________________________ |
| **Date** | _________________________________ |
| **Selected option** | ☐ Option A  ☐ Option B  ☐ Custom: _____________ |

---

**Contact**  
*[Your name]* · *[Email]* · *[Phone / WhatsApp]*  

*Dreams Creations — Operations Edition · Confidential*
