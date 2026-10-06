# Pre-deployment review — Dreams Creations ERP

Use this before first production deploy or a major release. Sign each section when complete.

**Release:** _______________  
**Reviewer:** _______________  
**Date:** _______________

---

## A. Application scope

| Item | Status | Notes |
|------|--------|-------|
| Production ERP in this repo (no shop UI) | ☐ | Shop is separate project |
| Finance module intent documented | ☐ | `MODULE-ARCHITECTURE.md` |
| Branch matches release (`main` vs `feature/finance-v2`) | ☐ | |

---

## B. Security

| Item | Status | Notes |
|------|--------|-------|
| `jwt.secret` is not default / committed | ☐ | |
| DB credentials not in git | ☐ | `application.properties` gitignored |
| `expose-link` false for reset & verify | ☐ | |
| SMTP enabled for auth emails | ☐ | |
| Shop integration key rotated from placeholder | ☐ | If integration used |
| HTTPS on public URL | ☐ | |
| CORS lists only real frontend origin(s) | ☐ | `app.cors.allowed-origins` |
| Admin bootstrap password changed after first login | ☐ | |

---

## C. Data & migrations

| Item | Status | Notes |
|------|--------|-------|
| All required SQL scripts applied | ☐ | See `backend/src/main/resources/db/` |
| Finance SQL (if enabled) | ☐ | module, payables, bank |
| Backup taken before deploy | ☐ | |
| Upload directory on persistent volume | ☐ | `app.upload.dir` |

---

## D. Configuration parity

| Item | Status | Notes |
|------|--------|-------|
| `modules.finance.enabled` (backend) = `VITE_FINANCE_MODULE_ENABLED` (frontend build) | ☐ | |
| `app.frontend.url` matches live site | ☐ | Email links |
| Auto-post flags reviewed | ☐ | AR, inventory/COGS |

---

## E. Build & runtime

| Item | Status | Notes |
|------|--------|-------|
| `mvnw package` succeeds | ☐ | |
| `npm run build` succeeds | ☐ | |
| `/api/health` UP on server | ☐ | |
| Reverse proxy / API routing verified | ☐ | |
| Logs monitored for startup errors | ☐ | |

---

## F. Functional smoke (by role)

| Role | Critical paths | Status |
|------|----------------|--------|
| Admin | Login → modules → batches, bills, staff, setup | ☐ |
| Manager | Same minus staff/setup | ☐ |
| Supervisor | Assignments, designs read | ☐ |
| Customer | Designs, quotes, my bills | ☐ |
| Finance (if on) | COA, journal, payables, bank, reports | ☐ | See finance UAT doc |

---

## G. UI / UX

| Item | Status | Notes |
|------|--------|-------|
| Sidebar shows all menu items (scroll if needed) | ☐ | Fixed scroll + Finance link when enabled |
| Mobile drawer menu complete | ☐ | |
| Finance portal sidebar scrolls on short screens | ☐ | |

---

## H. Known limitations (accept or defer)

- No Docker/CI in repo yet — manual deploy process
- Health endpoint is shallow (no DB probe)
- Java package name still `com.dreams.dreamscreations` (cosmetic)
- Manual SQL migrations (no Flyway/Liquibase)

---

## Issues log

| # | Severity | Description | Owner | Resolved |
|---|----------|-------------|-------|----------|
| 1 | | | | ☐ |

**Go / no-go:** _______________
