# Dreams Creations ERP
## Complete System Status, Multi-Tenant Estimate & Hostinger VPS Deployment

**Document version:** 1.0  
**Date:** 6 October 2026  
**Repository:** dreams-creations (GitHub: MUAKH93/dreams-creations)  
**Current development branch:** feature/finance-v2  

---

## Table of contents

1. Executive summary  
2. Architecture and products  
3. Complete module status  
4. Finance module (F1–F6)  
5. Shop and integrations  
6. Security and configuration  
7. Gaps and remaining work  
8. VPS deployment readiness  
9. Multi-tenant: is it enabled today?  
10. Multi-tenant implementation: time estimate  
11. Complete Hostinger VPS deployment process  
12. Post-deployment checklist  
13. Recommended release paths  

---

## 1. Executive summary

Dreams Creations ERP is a **single-factory, single-database** business application:

- **Backend:** Java 17, Spring Boot, REST API (port 8080)  
- **Frontend:** React, Vite, Ant Design (port 3000 in development)  
- **Database:** MySQL 8 (`dreams_creations_db`)  

| Area | Status |
|------|--------|
| Operations (production, sales, inventory, admin) | ~90–95% complete; suitable for VPS after config + SQL + smoke tests |
| Finance portal | Code complete through **Phase F6**; **UAT sign-off pending**; on feature branch |
| Online shop UI | **Not in this repo** — separate project (`feature/shop-v1` / dreams-creations-shop) |
| Multi-tenant | **Not implemented** |
| VPS deployment | **Documented and buildable**; **not automated** (no Docker/Nginx in repo); server setup is manual |

**Git state (at time of writing):** Branch `feature/finance-v2`, synced with `origin/feature/finance-v2` for committed work, but **many uncommitted local changes** (sidebar fixes, deployment docs, module hub, shop integration API, dashboard/inventory updates). Commit and push before treating any deploy as official.

---

## 2. Architecture and products

### 2.1 This repository (Production ERP)

After login, **Admin** and **Manager** use **Module hub** (`/modules`) to choose:

1. **Production & Operations** → dashboard, batches, dispatch, inventory, designs, customers, quotations, bills, alerts  
2. **Finance Portal** → `/finance` (only when `modules.finance.enabled=true`)

**Supervisor** and **Customer** use role-specific dashboards (no module hub).

### 2.2 Separate products (not merged into current branch UI)

| Product | Purpose |
|---------|---------|
| **dreams-creations-shop** | E-commerce storefront, cart, checkout (separate app, ports 3002/8082) |
| **prodflow-erp** (if used) | White-label copy of ERP — separate folder, DB, and ports — **not multi-tenant inside one app** |

### 2.3 Shop → ERP integration (backend only in ERP)

External shop can POST paid orders:

```
POST https://your-domain.com/api/integration/shop/orders/sync
Header: X-Shop-Integration-Key: <app.shop.integration.api-key>
```

Creates operational/finance records (e.g. bill `SHOP-{orderNumber}`) when configured.

---

## 3. Complete module status

### 3.1 Operations — implemented features

| Module | Backend | Frontend | Notes |
|--------|---------|----------|-------|
| Authentication (JWT) | Yes | Yes | Login, register, verify email, forgot/reset password |
| Roles | Yes | Yes | ADMIN, MANAGER, SUPERVISOR, CUSTOMER |
| Dashboard & charts | Yes | Yes | Role-specific dashboards; charts on admin/manager home |
| Production batches | Yes | Yes | Design phase, stages, flow tracking |
| Dispatch & assignments | Yes | Yes | Supervisor assignments |
| Inventory | Yes | Yes | Stock, adjustments, labels/print |
| Designs catalog | Yes | Yes | Images, categories, types, pricing/cost fields |
| Customers | Yes | Yes | Management portal |
| Quotations | Yes | Yes | Customer “My Quotes” |
| Bills & payments | Yes | Yes | Partial payments supported (recent fix on branch) |
| Alerts | Yes | Yes | Production/sales alerts |
| Staff (Admin) | Yes | Yes | |
| Factory setup (Admin) | Yes | Yes | Modules, stages, supervisors, etc. |
| Profile & photos | Yes | Yes | |
| Tutorials / guide | Yes | Yes | Operations + finance tabs |
| Activity log API | Yes | Page exists | Route `/activity` redirects to dashboard — **not linked in menu** |
| Analytics API | Yes | Page exists | Route `/analytics` redirects to dashboard — **embedded charts on dashboard instead** |
| Operations reports page | Yes | Orphaned | `/reports` redirects to finance when finance enabled |
| Health check | Yes | Yes | `GET /api/health` → `{"status":"UP"}` (no DB probe) |

### 3.2 Finance — implemented features (when enabled)

| Phase | Feature |
|-------|---------|
| F1 | Chart of accounts, manual journal entries |
| F2 | AR auto-post from operations bills/payments |
| F3 | Inventory & COGS auto-post (configurable) |
| F4 | Trial balance, GL, AR/AP aging, P&L, balance sheet |
| F5 | Payables (vendor bills/payments) |
| F6 | Bank accounts, statement lines, reconciliation |

**Official next step in product roadmap:** UAT sign-off and merge `feature/finance-v2` → `main`.

Required SQL (finance):

- `add-finance-module.sql`  
- `add-finance-payables.sql`  
- `add-finance-bank.sql`  

UAT document: `docs/finance-uat-checklist.md`  
Prep script: `scripts/finance-uat-prep.ps1`  

### 3.3 Testing and DevOps maturity

| Item | Status |
|------|--------|
| Unit/integration tests | Minimal (Spring context test only) |
| CI/CD | Not in repository |
| Docker | Not in repository |
| DB migrations tool (Flyway/Liquibase) | Not used — manual SQL scripts |
| Multi-tenant | Not present |

---

## 4. Finance module configuration

**Default (production-safe example):** `modules.finance.enabled=false` in `application.properties.example`.

**Enable for UAT/production (after sign-off):** copy from `application-finance.properties.example`:

```properties
modules.finance.enabled=true
modules.finance.auto-post-ar=true
modules.finance.auto-post-inventory=true
modules.finance.auto-post-ap=true
```

**Frontend build must match:**

```env
VITE_FINANCE_MODULE_ENABLED=true
```

Build with `false` on operations-only deploys.

---

## 5. Shop and integrations

- **No shop storefront** in this ERP frontend.  
- **Shop integration API** exists in codebase (may be uncommitted locally): sync orders into ERP/finance.  
- Configure matching API keys on ERP and shop projects.  
- Bootstrap script for shop project: `scripts/bootstrap-shop-project.ps1`  

---

## 6. Security and configuration (production)

| Setting | Production requirement |
|---------|------------------------|
| `jwt.secret` | Long random secret (≥32 chars), unique per environment |
| `spring.datasource.*` | Production MySQL; strong password |
| `app.frontend.url` | Public HTTPS URL (email links) |
| `app.cors.allowed-origins` | Your real frontend origin(s), comma-separated |
| `app.upload.dir` | Persistent disk path (not temp) |
| `app.password-reset.expose-link` | **false** |
| `app.email-verification.expose-link` | **false** |
| `app.mail.enabled` | **true** + SMTP (Hostinger email or other) |
| `app.shop.integration.api-key` | Strong random key if shop sync is used |
| HTTPS | Required on public VPS |

Files **not** in git (create on server): `backend/src/main/resources/application.properties`, `frontend/.env` (at build time).

---

## 7. Gaps and remaining work

### Before first VPS go-live (any path)

1. Commit/push release candidate; tag if desired.  
2. Run `docs/PRE-DEPLOYMENT-REVIEW.md` checklist.  
3. Apply all required SQL on server DB.  
4. Configure secrets, SMTP, CORS, uploads path.  
5. Nginx + systemd + TLS on VPS.  
6. Role-based smoke tests on live URL.  

### If finance is included

7. Complete `docs/finance-uat-checklist.md` (sections 0–9).  
8. Merge finance branch to `main` after sign-off.  

### Optional improvements (not blocking)

- Wire or remove Analytics and Activity Log pages in navigation.  
- Operations vs finance reports clarity.  
- DB health in `/api/health`.  
- Automated tests and Flyway.  
- Frontend code-splitting (large JS bundle).  

---

## 8. VPS deployment readiness

| Ready | Not ready / manual |
|-------|---------------------|
| `mvn package` and `npm run build` succeed | VPS provisioned and app installed (you do this) |
| `docs/DEPLOYMENT.md` | Nginx config in repo (write on server) |
| `docs/PRE-DEPLOYMENT-REVIEW.md` | systemd unit in repo (write on server) |
| Configurable CORS | TLS certificate setup |
| Optional `VITE_API_BASE_URL` | Monitoring, backups, log rotation |

**Recommended first deploy:** operations only from `main`, finance **off**, then enable finance after UAT.

---

## 9. Multi-tenant: is it enabled today?

**No.**

- One MySQL database per deployment.  
- One `spring.datasource.url`.  
- No `tenant_id` on entities.  
- No tenant resolution (subdomain/header).  
- **Separate factories/clients** = **separate deployments** (or separate folders like prodflow-erp), each with its own database — that is **multi-instance**, not **multi-tenant**.

---

## 10. Multi-tenant implementation: time estimate

Estimates assume **one experienced full-stack developer** familiar with this codebase, working **full-time**, including basic tests and one staging environment. Calendar time increases if part-time or if scope expands (billing, self-service signup, etc.).

### 10.1 Approach A — Shared database, `tenant_id` on rows (fastest)

| Work | Scope | Estimate |
|------|--------|----------|
| Design | Tenant model, resolution (subdomain or login field), migration strategy | 2–3 days |
| Backend | Tenant context filter, add `tenant_id` to all relevant entities/repos/services, data migration | 10–15 days |
| Frontend | Tenant-aware login/branding (optional subdomain) | 2–4 days |
| Provisioning | Admin creates tenant + seed data | 3–5 days |
| Security review | Cross-tenant leakage tests | 3–5 days |
| **Total** | | **~4–6 weeks** |

**Pros:** One DB to backup; cheaper hosting.  
**Cons:** Risk of data leaks if queries miss tenant filter; harder compliance story.

### 10.2 Approach B — Database per tenant (strongest isolation)

| Work | Scope | Estimate |
|------|--------|----------|
| Design | Tenant registry DB + dynamic datasource routing | 3–5 days |
| Backend | Routing layer, per-tenant migrations, connection pooling | 15–20 days |
| Provisioning | Create DB, run SQL, register tenant | 5–8 days |
| Frontend | Tenant selection / subdomain mapping | 3–5 days |
| Ops | Backup per DB, connection limits, monitoring | 5–7 days |
| **Total** | | **~6–10 weeks** |

**Pros:** Strong isolation; easier “export one client”.  
**Cons:** More ops complexity; many MySQL databases on one VPS need sizing.

### 10.3 Approach C — Keep separate VPS/deploy per client (what you have now)

| Work | Scope | Estimate |
|------|--------|----------|
| Automation | Scripts/templates to clone deploy + DB | 3–5 days |
| **Total** | | **~1 week** for repeatable deploy playbook |

**Pros:** No rewrite; matches current architecture.  
**Cons:** Not true multi-tenant SaaS; N servers for N clients.

### 10.4 Recommendation

- For **2–5 factories** on Hostinger: **Approach C** (automated separate deploys) is usually best cost/time.  
- For **SaaS with many tenants on one URL**: plan **Approach A or B**; budget **1.5–2.5 months** minimum before production multi-tenant.

---

## 11. Complete Hostinger VPS deployment process

Assumptions:

- Hostinger **VPS** (Linux, Ubuntu 22.04 or 24.04) — not shared web hosting alone (you need Java + MySQL + long-running API).  
- Domain pointed to VPS IP (e.g. `erp.yourdomain.com`).  
- Deploy **operations only** first (`main`, finance off) unless UAT is complete.

### Phase 1 — Order and access Hostinger VPS

1. Purchase Hostinger VPS plan with enough RAM (**minimum 2 GB**, **4 GB recommended** for MySQL + Java).  
2. Choose **Ubuntu** as OS.  
3. Note **public IP**, **root password** or SSH key from hPanel.  
4. (Optional) Point **A record** `erp.yourdomain.com` → VPS IP in Hostinger DNS.

### Phase 2 — Initial server setup (SSH)

From your PC:

```bash
ssh root@YOUR_VPS_IP
```

On the server:

```bash
apt update && apt upgrade -y
timedatectl set-timezone Asia/Karachi
adduser deploy
usermod -aG sudo deploy
# Copy your SSH key to /home/deploy/.ssh/authorized_keys
ufw allow OpenSSH
ufw allow 80
ufw allow 443
ufw enable
```

Use `deploy` user for app work afterward.

### Phase 3 — Install Java 17, MySQL 8, Nginx

```bash
sudo apt install -y openjdk-17-jdk nginx mysql-server
java -version
sudo mysql_secure_installation
```

Create database and user:

```sql
sudo mysql -u root -p
CREATE DATABASE dreams_creations_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'dreams_app'@'localhost' IDENTIFIED BY 'STRONG_PASSWORD_HERE';
GRANT ALL PRIVILEGES ON dreams_creations_db.* TO 'dreams_app'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Phase 4 — Load database schema/data

1. Copy SQL files from `backend/src/main/resources/db/` to the server (SCP, SFTP, or git clone on server).  
2. Run scripts in order appropriate to your situation (fresh install vs upgrade).  
3. **If finance is enabled**, also run finance trio scripts.  
4. Verify tables exist: `mysql -u dreams_app -p dreams_creations_db -e "SHOW TABLES;"`

### Phase 5 — Build application (on PC or on server)

**On your development machine (recommended):**

```powershell
cd dreams-creations\backend
.\mvnw.cmd -DskipTests package

cd ..\frontend
# For operations-only:
# VITE_FINANCE_MODULE_ENABLED=false in .env
npm ci
npm run build
```

Upload to VPS:

- `backend/target/*.jar` → e.g. `/opt/dreams-creations/app.jar`  
- `frontend/dist/*` → e.g. `/var/www/dreams-creations/`  
- Create uploads directory: `/var/lib/dreams-creations/uploads`

**Alternative:** Clone repo on VPS, install Node only for build, then remove if desired.

### Phase 6 — Backend configuration on VPS

Create `/opt/dreams-creations/application.properties` (or use env vars):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dreams_creations_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=dreams_app
spring.datasource.password=STRONG_PASSWORD_HERE

jwt.secret=GENERATE_A_LONG_RANDOM_SECRET

app.frontend.url=https://erp.yourdomain.com
app.cors.allowed-origins=https://erp.yourdomain.com

app.upload.dir=/var/lib/dreams-creations/uploads

app.password-reset.expose-link=false
app.email-verification.expose-link=false

app.mail.enabled=true
app.mail.from=noreply@yourdomain.com
spring.mail.host=smtp.hostinger.com
spring.mail.port=587
spring.mail.username=your-mailbox@yourdomain.com
spring.mail.password=YOUR_MAIL_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

modules.finance.enabled=false
```

Adjust Hostinger SMTP host/port from their current documentation.

### Phase 7 — systemd service for Spring Boot

`/etc/systemd/system/dreams-creations.service`:

```ini
[Unit]
Description=Dreams Creations ERP API
After=network.target mysql.service

[Service]
User=deploy
WorkingDirectory=/opt/dreams-creations
ExecStart=/usr/bin/java -jar /opt/dreams-creations/app.jar --spring.config.additional-location=file:/opt/dreams-creations/application.properties
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable dreams-creations
sudo systemctl start dreams-creations
sudo systemctl status dreams-creations
curl -s http://127.0.0.1:8080/api/health
```

### Phase 8 — Nginx reverse proxy + static frontend

`/etc/nginx/sites-available/dreams-creations`:

```nginx
server {
    listen 80;
    server_name erp.yourdomain.com;

    root /var/www/dreams-creations;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 12M;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/dreams-creations /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Phase 9 — HTTPS (SSL)

On Ubuntu with Hostinger VPS, **Certbot** is common:

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d erp.yourdomain.com
```

Or use Hostinger’s SSL tools in hPanel if they provide managed SSL for VPS.

Verify: `https://erp.yourdomain.com` loads SPA; login works; API calls go to `/api` (same origin — no CORS issues).

### Phase 10 — First login and hardening

1. Log in as Admin (bootstrap user from your DB/seed process).  
2. **Change default admin password immediately.**  
3. Create Manager/Supervisor test users.  
4. Test: create batch, bill, upload design image, email reset (if mail on).  
5. Set up **daily MySQL dump** cron and backup `/var/lib/dreams-creations/uploads`.  

Example backup cron:

```bash
0 2 * * * mysqldump -u dreams_app -p'PASSWORD' dreams_creations_db | gzip > /backups/db-$(date +\%F).sql.gz
```

### Phase 11 — Enable finance later (optional)

1. Complete UAT on staging.  
2. Merge `feature/finance-v2` → `main`.  
3. Run finance SQL on production DB.  
4. Set `modules.finance.enabled=true` and auto-post flags.  
5. Rebuild frontend with `VITE_FINANCE_MODULE_ENABLED=true`, redeploy `dist/`, restart API.  

### Hostinger-specific notes

- **Shared web hosting** (without VPS) generally **cannot** run this stack as-is; use **VPS** or **Cloud** with root access.  
- Use Hostinger **business email** or SMTP credentials for `spring.mail.*`.  
- If RAM is tight, tune MySQL `innodb_buffer_pool_size` and monitor with `htop`.  
- Keep **8080** bound to localhost only; public traffic only via Nginx 443.  

---

## 12. Post-deployment checklist

- [ ] HTTPS works; HTTP redirects to HTTPS  
- [ ] `/api/health` returns UP through Nginx  
- [ ] Login/logout all roles  
- [ ] File uploads persist after restart  
- [ ] Email verification/reset sends mail (not exposed in JSON)  
- [ ] Backups tested (restore drill once)  
- [ ] Finance UAT (if finance enabled)  
- [ ] PRE-DEPLOYMENT-REVIEW.md signed off  

---

## 13. Recommended release paths

| Path | When | Branch / flags |
|------|------|----------------|
| **A — Operations only** | First factory go-live on Hostinger | `main`, finance off |
| **B — ERP + finance** | After UAT sign-off | Merge finance → `main`, finance on |
| **C — ERP + external shop** | When shop project is deployed | ERP + shop + integration key |

---

## Document references in repository

- `docs/DEPLOYMENT.md`  
- `docs/PRE-DEPLOYMENT-REVIEW.md`  
- `docs/finance-uat-checklist.md`  
- `docs/MODULE-ARCHITECTURE.md`  
- `README.md`  

---

**Prepared for:** Dreams Creations / Rovexa Technologies  
**Disclaimer:** Hostinger product names, SMTP hosts, and hPanel steps may change; confirm against Hostinger’s current VPS documentation.
