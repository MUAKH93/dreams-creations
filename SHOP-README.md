# Dreams Creations Online Shop

Standalone e-commerce app (extracted from ERP shop module).

## Dev ports

- Frontend: 3002
- Backend: 8082

## ERP integration

Shop orders sync to the main ERP for inventory and finance.

- ERP URL: http://localhost:8080
- Endpoint: POST /api/integration/shop/orders/sync
- Header: X-Shop-Integration-Key (must match ERP app.shop.integration.api-key)

See parent repo docs/MODULE-ARCHITECTURE.md.

## Run

Backend: cd backend; .\mvnw.cmd spring-boot:run
Frontend: cd frontend; npm install; npm run dev

Storefront: http://localhost:3002/store
