# Deployment guide — Dreams Creations ERP

**Branch:** `feature/finance-v2` (finance UAT) or `main` (operations-only)

This app is a **Spring Boot API** + **React SPA**. Production should serve the built frontend and proxy `/api` to the backend on one public origin, or configure CORS + `VITE_API_BASE_URL`.

---

## 1. Build artifacts

```powershell
cd backend
.\mvnw.cmd -DskipTests package

cd ..\frontend
npm ci
npm run build
```

Output: `frontend/dist/` (static files). Backend JAR: `backend/target/*.jar`.

---

## 2. Database

1. Create MySQL database `dreams_creations_db` (utf8mb4).
2. Apply scripts in `backend/src/main/resources/db/` in order for your environment (fresh install vs upgrades). Finance requires at minimum:
   - `add-finance-module.sql`
   - `add-finance-payables.sql`
   - `add-finance-bank.sql`
3. Set `spring.jpa.hibernate.ddl-auto=validate` in production (never `update` on live data without review).

---

## 3. Backend configuration

Copy `application.properties.example` → `application.properties` on the server.

| Setting | Production |
|---------|------------|
| `spring.datasource.*` | Production MySQL host, user, strong password |
| `jwt.secret` | Random string ≥ 32 characters (unique per environment) |
| `app.frontend.url` | Public app URL (password reset / verify emails) |
| `app.cors.allowed-origins` | Same as public app URL(s), comma-separated |
| `app.upload.dir` | Persistent disk path (not temp) |
| `app.password-reset.expose-link` | **false** |
| `app.email-verification.expose-link` | **false** |
| `app.mail.enabled` | **true** + SMTP settings |
| `modules.finance.enabled` | **true** only after finance UAT sign-off |
| `app.shop.integration.api-key` | Strong random key (if shop integration used) |

Health check: `GET /api/health` → `{"status":"UP"}`.

---

## 4. Frontend configuration

Copy `frontend/.env.example` → `frontend/.env` before build.

| Variable | Production |
|----------|------------|
| `VITE_FINANCE_MODULE_ENABLED` | Must match `modules.finance.enabled` |
| `VITE_API_BASE_URL` | Omit if reverse proxy serves `/api` on same host; else full API base e.g. `https://api.example.com/api` |

---

## 5. Recommended hosting pattern

**Same origin (recommended):**

- Nginx (or similar) serves `frontend/dist` for `/`
- Proxies `/api` → `http://127.0.0.1:8080`
- TLS certificate on the public hostname

**Split origin:**

- Host SPA on CDN/static bucket
- API on separate subdomain
- Set `app.cors.allowed-origins` and build with `VITE_API_BASE_URL`

---

## 6. Post-deploy smoke test

1. Login as Admin → Module hub → Operations dashboard loads.
2. Sidebar scrolls; all role menus visible (Admin: Staff, Setup, Profile at bottom).
3. Finance (if enabled) → overview, chart of accounts, one report.
4. Create bill → payment → no 500 errors.
5. Upload design image → file appears under `app.upload.dir`.
6. Password reset / verify email sends mail (links not returned in JSON).

---

## 7. Related docs

- [PRE-DEPLOYMENT-REVIEW.md](./PRE-DEPLOYMENT-REVIEW.md) — checklist before go-live
- [finance-uat-checklist.md](./finance-uat-checklist.md) — finance sign-off
- [MODULE-ARCHITECTURE.md](./MODULE-ARCHITECTURE.md) — shop vs ERP boundaries
