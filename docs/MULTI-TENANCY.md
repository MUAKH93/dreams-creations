# Multi-tenancy (database per tenant)

Each **tenant** is a separate MySQL database with the **same schema**. The API routes JDBC connections using the active tenant id.

## Enable

1. Create one database per tenant (e.g. `dreams_creations_db`, `dreams_acme_db`).
2. Run the same SQL migration scripts on **each** database.
3. In `application.properties`:

```properties
app.multitenancy.enabled=true
app.multitenancy.default-tenant-id=default

app.multitenancy.tenants.default.display-name=Dreams Creations (Main)
app.multitenancy.tenants.default.datasource.url=jdbc:mysql://localhost:3306/dreams_creations_db?useSSL=false&serverTimezone=UTC
app.multitenancy.tenants.default.datasource.username=root
app.multitenancy.tenants.default.datasource.password=YOUR_PASSWORD

app.multitenancy.tenants.acme.display-name=Acme Factory
app.multitenancy.tenants.acme.datasource.url=jdbc:mysql://localhost:3306/dreams_acme_db?useSSL=false&serverTimezone=UTC
app.multitenancy.tenants.acme.datasource.username=root
app.multitenancy.tenants.acme.datasource.password=YOUR_PASSWORD
```

When `enabled=true`, configure **all** tenants under `app.multitenancy.tenants.*` (do not rely on `spring.datasource` alone).

## Client / API contract

| Mechanism | Usage |
|-----------|--------|
| `GET /api/auth/tenants` | Lists tenant ids and display names (login dropdown) |
| Header `X-Tenant-ID` | Required on API calls when multi-tenancy is enabled (except `/api/auth/tenants`) |
| JWT claim `tenantId` | Set at login; must match header if both are sent |
| Query `?tenant=` | Used on verify-email and password-reset links |

## Frontend

- Login shows an organization selector when more than one tenant is returned.
- `localStorage.tenantId` + axios sends `X-Tenant-ID` on every request.

## Shop integration

`POST /api/integration/shop/orders/sync` requires:

- `X-Shop-Integration-Key`
- `X-Tenant-ID` (target factory database)

## Uploads

Files are stored under `{app.upload.dir}/{tenantId}/`.

## Scheduled jobs

Daily alert checks run **once per configured tenant**.

## Platform super-admin

Configure `app.platform.super-admin.*` in `application.properties` (not stored in tenant databases).

| Endpoint | Purpose |
|----------|---------|
| `POST /api/auth/platform/login` | Super-admin JWT (`SUPER_ADMIN` role) |
| `GET /api/platform/tenants` | List tenants |
| `POST /api/platform/tenant-admins` | Create an **ADMIN** user in a client tenant database |

Example create client admin:

```http
POST /api/platform/tenant-admins
Authorization: Bearer <super-admin-token>
Content-Type: application/json

{
  "tenantId": "acme",
  "username": "AcmeAdmin",
  "password": "ChangeMe-Strong-Password-1!",
  "email": "admin@acme.example",
  "firstName": "Acme",
  "lastName": "Admin"
}
```

On startup, `app.bootstrap.tenant-admins.<tenantId>.*` upserts the factory admin (e.g. Dreams Creations).

## Single-tenant (default)

`app.multitenancy.enabled=false` — existing `spring.datasource.*` only; tenant id defaults to `default` and no extra header is required.
