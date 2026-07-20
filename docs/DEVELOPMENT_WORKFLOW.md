# Development Workflow — Parallel Modules

How to build **finance**, **shop**, and other modules without disturbing the stable Operations system on `main`.

---

## Branch strategy

```text
main                 → Production (Operations v1.0) — factory live
feature/finance-v2   → Finance module (current)
feature/shop-v2      → Customer shop (future, after or parallel to finance)
feature/payments-v2  → Online payments (after shop)
```

### Rules

1. **Never** build new modules directly on `main`.
2. Merge to `main` only after staging UAT and signed checklist.
3. Tag releases: `v1.0` (operations), `v2.0-finance`, `v2.1-shop`, etc.
4. One feature branch per major module.

---

## Environments

| Environment | Branch | Database | Purpose |
|-------------|--------|----------|---------|
| Production | `main` | `dreams_creations_db` | Live factory |
| Staging | `feature/finance-v2` | `dreams_creations_staging` | Test finance safely |
| Local | your feature branch | local MySQL | Development |

---

## Feature flags

### Backend (`application.properties`)

```properties
modules.finance.enabled=false
modules.finance.auto-post-ar=false
modules.finance.auto-post-inventory=false
modules.shop.enabled=false
```

On **`feature/finance-v2`** locally, set `modules.finance.enabled=true`.

### Frontend (`frontend/.env`)

Copy `.env.example` to `.env`:

```env
VITE_FINANCE_MODULE_ENABLED=true
```

On **`main`**, leave `false` (or omit — defaults to false).

Both backend and frontend flags must be `true` for finance menu and APIs to appear.

---

## Git worktree (optional — run two branches side by side)

```powershell
cd C:\Users\Muhammad Umair Ayub\dreams-creations
git worktree add ..\dreams-creations-finance feature/finance-v2
```

| Folder | Branch | Suggested ports |
|--------|--------|-----------------|
| `dreams-creations` | `main` | Frontend 3000, Backend 8080 |
| `dreams-creations-finance` | `feature/finance-v2` | Frontend 3001, Backend 8081 |

Use a **separate staging database** for the finance worktree.

---

## Starting finance development

```powershell
git checkout feature/finance-v2
git pull

# 1. Staging database
# Run in MySQL Workbench:
#   backend/src/main/resources/db/add-finance-module.sql

# 2. Backend config (application.properties)
#   modules.finance.enabled=true

# 3. Frontend
cd frontend
copy .env.example .env
# Edit: VITE_FINANCE_MODULE_ENABLED=true
npm run dev

# 4. Backend
cd backend
.\mvnw.cmd spring-boot:run
```

Verify: `GET /api/modules` → `{ "finance": { "enabled": true } }`  
Verify: Finance menu appears for Admin/Manager.

---

## SQL migrations

- New module SQL files live in `backend/src/main/resources/db/`
- Naming: `add-finance-module.sql`, `add-finance-phase-f2-ar.sql`, etc.
- **Never** alter existing operational tables in breaking ways on a feature branch without a migration plan for merge.
- Always backup production DB before running migrations on go-live.

---

## Merge procedure (feature → main)

1. Freeze scope on feature branch  
2. Complete UAT checklist in `docs/FINANCE_MODULE_SPEC.md`  
3. `git checkout main && git pull`  
4. `git merge feature/finance-v2`  
5. Resolve conflicts; run full build  
6. Backup production DB  
7. Run new SQL migrations on production  
8. Deploy backend + frontend with `modules.finance.enabled=true`  
9. Tag: `git tag v2.0-finance`  
10. Monitor 90-day warranty period  

---

## Rollback

If go-live fails:

1. Redeploy previous app version (pre-merge tag)  
2. Restore DB from backup taken before migration  
3. Set `modules.finance.enabled=false` if partial deploy  

---

## Next modules (same pattern)

| Module | Branch | API prefix | SQL prefix |
|--------|--------|------------|------------|
| Finance | `feature/finance-v2` | `/api/finance/` | `add-finance-*.sql` |
| Shop | `feature/shop-v2` | `/api/shop/` | `add-shop-*.sql` |
| Payments | `feature/payments-v2` | `/api/payments/gateway/` | `add-payments-*.sql` |

---

*See also: `docs/FINANCE_MODULE_SPEC.md`, `docs/CLIENT_PROPOSAL.md`*
