# Dreams Creations

Factory management system for suit design, production, inventory, and sales.

## Repository layout

```
dreams-creations/
├── backend/     Spring Boot API (Java 17, port 8080)
├── frontend/    React + Vite + Ant Design (port 3000)
└── README.md
```

## Prerequisites

- Java 17
- Maven (or use `backend/mvnw.cmd` on Windows)
- Node.js 18+
- MySQL 8 (e.g. XAMPP or MySQL Workbench)
- Database: `dreams_creations_db`

## Backend setup

```powershell
cd backend
copy src\main\resources\application.properties.example src\main\resources\application.properties
```

Edit `application.properties` with your MySQL password and a secure JWT secret.

Run SQL migration scripts from `backend/src/main/resources/db/` in MySQL Workbench as needed.

```powershell
.\mvnw.cmd spring-boot:run
```

API: http://localhost:8080

## Frontend setup

```powershell
cd frontend
npm install
npm run dev
```

App: http://localhost:3000

The Vite dev server proxies `/api` to the backend.

## Roles

- **ADMIN** / **MANAGER** — full access, reports, inventory
- **SUPERVISOR** — production and dispatch
- **CUSTOMER** — orders and bills

## Uploads

Design images are stored outside the repo at `%USERPROFILE%\dreams-creations\uploads` (configurable via `app.upload.dir`).

## Production flow

Designing → Filling (optional) → Cutting & Stitching
