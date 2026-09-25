# Deployment

Two ways to run JAVALIVE in production: Docker Compose (recommended — self-contained, matches
what's tested below) or natively on Windows (Spring Boot as its own process/service, nginx or IIS
serving the built frontend and reverse-proxying the API).

Either way, start by generating real secrets — **never reuse the dev defaults in
`application.yml`**:

```
openssl rand -base64 48   # JWT_SECRET
openssl rand -base64 32   # ENCRYPTION_KEY (must decode to exactly 32 bytes)
```

## Option A — Docker Compose

1. `cp deployment/.env.example deployment/.env` and fill in every value (DB credentials, SMTP,
   the two secrets above, your real domain for `CORS_ALLOWED_ORIGINS`/`FRONTEND_URL`).
2. From the `deployment/` directory: `docker compose --env-file .env up -d --build`
3. This builds and starts three containers: `mysql` (MySQL 8, Flyway migrates the schema
   automatically on first backend boot — starts empty, see "Migrating real data" below if you
   need to load an existing dataset), `backend` (Spring Boot, port 8080), `frontend` (nginx
   serving the built Vue SPA on port 80, reverse-proxying `/api/*` and `/storage/*` to `backend`).
4. Visit `http://localhost` (or your domain once DNS/TLS is in front of it). Health check:
   `http://localhost:8080/actuator/health`.

Put a real TLS-terminating reverse proxy (Caddy, another nginx, a cloud load balancer) in front of
the `frontend` container for anything internet-facing — this compose file is HTTP-only.

## Option B — Native Windows

**Backend**:
1. `cd backend && mvn clean package -DskipTests` — produces `target/backend-0.0.1-SNAPSHOT.jar`.
2. Set every variable from `.env.example` as a real Windows environment variable (System Properties
   → Environment Variables, or `setx NAME value` per variable, or a service manager's env config).
3. Run: `java -jar backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
   - To run as a proper Windows service instead of a console app, wrap it with
     [NSSM](https://nssm.cc/) (`nssm install JavaliveBackend "C:\path\to\jre\bin\java.exe" "-jar C:\path\to\backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod"`, set the env vars in NSSM's
     "Environment" tab) or Windows' built-in `sc.exe create` with a similar wrapper.

**Frontend**:
1. `cd frontend && npm install && npm run build` — produces `dist/`.
2. Serve `dist/` as static files with nginx-for-Windows or IIS, reverse-proxying `/api/` and
   `/storage/` to `http://localhost:8080/` (same two `location` blocks as `nginx.conf` in this
   directory — reuse that file directly if using nginx-for-Windows) and falling back to
   `index.html` for every other path (Vue Router history mode).

## Option C — Native Linux VPS (systemd + nginx)

**Prerequisites**: Java 21 JDK, MySQL 8 (or MariaDB), nginx, all installed on the VPS already.

**Backend**:
1. Build the jar (either on the VPS or locally and `scp` it over): `cd backend && mvn clean package -DskipTests` — produces `target/backend-0.0.1-SNAPSHOT.jar`.
2. Create the database: `mysql -u root -p -e "CREATE DATABASE javalive_db; CREATE USER 'javalive'@'localhost' IDENTIFIED BY 'a-real-password'; GRANT ALL ON javalive_db.* TO 'javalive'@'localhost';"`
3. Put the jar somewhere permanent, e.g. `/opt/javalive/backend.jar`, and create `/opt/javalive/storage/public` (writable by whichever user runs the service) for uploads.
4. Create `/etc/javalive-backend.env` (root-only readable — `chmod 600`) with every variable from `.env.example`, plus:
   ```
   DB_URL=jdbc:mysql://localhost:3306/javalive_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
   DB_USERNAME=javalive
   DB_PASSWORD=a-real-password
   STORAGE_PUBLIC_PATH=/opt/javalive/storage/public
   PORT=8080
   CORS_ALLOWED_ORIGINS=https://yourdomain.com
   FRONTEND_URL=https://yourdomain.com
   JWT_SECRET=...          # openssl rand -base64 48
   ENCRYPTION_KEY=...      # openssl rand -base64 32
   ```
5. Create a systemd unit, `/etc/systemd/system/javalive-backend.service`:
   ```ini
   [Unit]
   Description=Javalive Backend
   After=network.target mysql.service

   [Service]
   EnvironmentFile=/etc/javalive-backend.env
   ExecStart=/usr/bin/java -jar /opt/javalive/backend.jar --spring.profiles.active=prod
   Restart=on-failure
   User=javalive

   [Install]
   WantedBy=multi-user.target
   ```
6. `sudo systemctl daemon-reload && sudo systemctl enable --now javalive-backend` — then `journalctl -u javalive-backend -f` to watch it boot.

**Frontend**:
1. `cd frontend && npm install && npm run build` — produces `dist/`.
2. Copy `dist/`'s contents to nginx's web root, e.g. `/var/www/javalive/`.
3. Point an nginx server block at it, reverse-proxying `/api/` and `/storage/` to the backend on
   `127.0.0.1:8080` (**not** `http://backend:8080` — that hostname only resolves inside the Docker
   Compose setup) and falling back to `index.html` for everything else:
   ```nginx
   server {
       listen 80;
       server_name yourdomain.com;
       root /var/www/javalive;
       index index.html;
       client_max_body_size 25m;

       location /api/ {
           proxy_pass http://127.0.0.1:8080/api/;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
       location /storage/ {
           proxy_pass http://127.0.0.1:8080/storage/;
           proxy_set_header Host $host;
       }
       location / {
           try_files $uri $uri/ /index.html;
       }
   }
   ```
4. `sudo certbot --nginx -d yourdomain.com` for free TLS (assuming Certbot is installed) — this
   rewrites the block above to redirect HTTP→HTTPS automatically.

## Migrating real data instead of starting empty

If you're deploying with the existing migrated dataset (`javalive_db`) rather than a fresh empty
schema: after the target MySQL instance is up but *before* the backend's first boot (so Flyway's
baseline doesn't conflict), restore a `mysqldump` of your `javalive_db` into it, then start the
backend normally — Flyway will see the schema already at its current version and skip migrations.
See `data-migration/` for how that dataset was originally produced from the source app's dump.

## Verifying the deployment

- `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`.
- Log in with a real admin account at `/admin/login` on the frontend.
- Confirm scheduled jobs are running: check backend logs a few minutes after boot for
  `CryptoPriceScheduler`/`InvestmentRoiScheduler`/etc. activity (see
  `docs/parity-checklist.md`'s Phase 6 section for what each job does and how it was verified).
- Confirm file uploads work (KYC doc, deposit proof) — proves the `/storage` volume/reverse-proxy
  path is wired correctly end to end.
