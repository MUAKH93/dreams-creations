# Dreams Creations — Deployment Guide

This guide covers a **pilot deployment** for factory use (office LAN or a small VPS). You can keep developing on your PC and push updates on a schedule.

---

## Barcode scanning (Phase 8 preview)

**No special hardware integration is required.** A standard USB barcode scanner acts like a keyboard — it types the barcode into whichever input field is focused.

| Scanner type | Works? | Notes |
|--------------|--------|-------|
| USB wired scanner | Yes | Plug in, scan into a search/SKU field |
| Bluetooth scanner (HID mode) | Yes | Pairs as keyboard |
| Phone camera | Later | Needs a camera library in the browser |

Phase 8 will add a **scan field** on Bills and Inventory pages. Until then, you can type or paste barcodes manually.

---

## Deployment options

| Option | Best for | Cost |
|--------|----------|------|
| **Factory PC (LAN)** | Office-only access, same Windows machine | Free |
| **Dedicated mini-server on LAN** | Multiple PCs on the factory network | Low |
| **VPS (Hetzner, DigitalOcean, etc.)** | Remote access + HTTPS | ~$5–10/month |

**Recommendation:** Start with a **factory LAN pilot** on one office PC or mini-server.

---

## Pre-deploy checklist

### 1. Database (MySQL)

- [ ] MySQL 8 running on the server (not only on your laptop)
- [ ] Database created: `dreams_creations_db`
- [ ] Run **all** SQL scripts from `backend/src/main/resources/db/` in order (including latest `add-phase7.sql`)
- [ ] Set up **daily backup** (see Backup section below)
- [ ] Do **not** expose MySQL port 3306 to the internet

### 2. Backend configuration

Copy and edit `application.properties` on the server:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dreams_creations_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_db_user
spring.datasource.password=STRONG_PASSWORD_HERE

jwt.secret=CHANGE_TO_A_LONG_RANDOM_STRING_AT_LEAST_32_CHARS
jwt.expiration=86400000

app.upload.dir=C:/dreams-creations/uploads
app.frontend.url=http://YOUR_SERVER_IP:3000
app.password-reset.expose-link=false

# Turn OFF debug in production
logging.level.com.dreams.dreamscreations.security=INFO
logging.level.org.springframework.security=INFO
```

### 3. Build backend JAR

```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
```

JAR output: `backend/target/dreams-creations-0.0.1-SNAPSHOT.jar`

Run:

```powershell
java -jar target/dreams-creations-0.0.1-SNAPSHOT.jar
```

For a persistent service, use **NSSM** or **Windows Task Scheduler** to start on boot.

### 4. Build frontend

```powershell
cd frontend
npm install
npm run build
```

Output: `frontend/dist/` (static files)

**Serve `dist/`** with one of:

- **IIS** on Windows (recommended for LAN)
- **Nginx** (if using Linux VPS)
- `npx serve dist` for quick testing only

Configure the web server to proxy `/api` → `http://localhost:8080` (same as Vite dev proxy).

### 5. Uploads folder

Create and back up:

```
C:\dreams-creations\uploads
```

(or the path set in `app.upload.dir`). Design images live here — include in backups.

### 6. Security

- [ ] Strong passwords for Admin and Manager accounts
- [ ] HTTPS if accessed outside LAN (Let's Encrypt on VPS, or internal CA)
- [ ] `app.password-reset.expose-link=false` in production
- [ ] Configure SMTP for password-reset emails (optional; or admin resets passwords manually)

---

## Factory LAN setup (Windows + XAMPP)

### Architecture

```
Staff PCs (browser) → http://192.168.x.x:80
                           ↓
                    IIS / static frontend
                           ↓ /api proxy
                    Spring Boot :8080
                           ↓
                    MySQL (XAMPP) :3306
```

### Steps

1. **Install** Java 17, Node.js (for builds), XAMPP MySQL on the server PC.
2. **Import** database and run all migration SQL scripts.
3. **Copy** `dreams-creations` repo to the server (or clone from GitHub).
4. **Configure** `application.properties` with server paths.
5. **Build** backend JAR and frontend `dist/`.
6. **Run** backend JAR (port 8080).
7. **Point IIS** (or similar) at `frontend/dist` with API reverse proxy.
8. **Open firewall** port 80 (or 3000) only on the LAN — not to the public internet unless you add HTTPS and hardening.
9. **Test** from another PC: `http://SERVER_IP/login`

### IIS API proxy (summary)

Install URL Rewrite + Application Request Routing. Add a rule:

- Pattern: `^api/(.*)`
- Rewrite to: `http://localhost:8080/api/{R:1}`

---

## Update routine (after pilot is live)

Use two environments:

```
Your dev PC  →  test changes locally
      ↓
Pilot server →  deploy when stable (e.g. Friday evening)
```

**Each update:**

1. Test locally (backend restart + frontend hard refresh)
2. Run any **new `.sql` scripts** on prod DB first
3. Deploy new backend JAR and restart Spring Boot
4. Deploy new `frontend/dist/` build
5. Hard-refresh browsers (Ctrl+F5)
6. Tag the release: `git tag v1.1.0`

**Rule:** Never change prod DB by hand — always use the `add-*.sql` scripts in the repo.

---

## Backup

### Database (daily)

```powershell
"C:\xampp\mysql\bin\mysqldump.exe" -u root -p dreams_creations_db > C:\backups\dreams_%date%.sql
```

Or use MySQL Workbench → Server → Data Export on a schedule.

### Uploads (weekly)

Copy `C:\dreams-creations\uploads` to backup storage or cloud.

---

## Smoke tests after deploy

| Test | Expected |
|------|----------|
| `GET http://SERVER:8080/api/health` | `{"status":"UP"}` |
| Login as Admin | Management dashboard loads |
| Login as Supervisor | Assignments page loads |
| Login as Customer | Designs catalog loads |
| Create a bill | Stock deducts, bill appears |
| Analytics page | Charts and supervisor table load |
| Upload design image | Image displays after refresh |

---

## What's in v1.0 (current)

- Full production → inventory → billing loop
- Role portals (Admin, Manager, Supervisor, Customer)
- Quotations, discounts, reports, activity log
- Analytics (supervisor performance, top designs/customers, profitability)
- Forgot password, responsive layout

## Planned for v1.1+

- Barcode scan fields on Bills / Inventory
- Automated backup reminders
- Email for password reset (SMTP)
- Mobile app (optional, later)

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `ddl-auto=validate` fails on startup | Run missing SQL migration on prod DB |
| Frontend shows "Cannot reach backend" | Check Spring Boot is running; verify API proxy |
| Images missing | Check `app.upload.dir` exists and is writable |
| 401 on every action | JWT secret changed — users must re-login |
| CORS errors | Backend `SecurityConfig` allowed origins must include frontend URL |

---

## GitHub repository

https://github.com/MUAKH93/dreams-creations.git

Clone on server:

```powershell
git clone https://github.com/MUAKH93/dreams-creations.git
cd dreams-creations
```
