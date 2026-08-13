# 📌 Production Database Configuration Note — RentersReady

> **Temporary In-Memory Fallback Active:** RentersReady is currently configured with an **H2 in-memory database fallback** (`jdbc:h2:mem:rentersready`). This allows the application to deploy and run instantly on Google Cloud Run without needing an external database set up right away.
> 
> **REQUIRED BEFORE GOING LIVE TO REAL USERS:**
> In-memory data will reset when Cloud Run instances restart or scale down to 0. Before launching the app to real landlords/tenants, you **must connect a persistent PostgreSQL database** (e.g., from [Neon.tech](https://neon.tech) or Google Cloud SQL).

---

## 🛠️ How to Connect Production PostgreSQL (Neon.tech / Cloud SQL)

1. Create a free PostgreSQL database at [Neon.tech](https://neon.tech) named `rentersready-prod`.
2. Add these repository secrets in **GitHub > Settings > Secrets and variables > Actions**:

| Secret Name | Value | Example |
|-------------|-------|---------|
| `RENTERSREADY_DB_URL` | `jdbc:postgresql://<host>/<dbname>?sslmode=require` | `jdbc:postgresql://ep-sample-12345.eu-central-1.aws.neon.tech/neondb?sslmode=require` |
| `RENTERSREADY_DB_USERNAME` | Your DB Username | `neondb_owner` |
| `RENTERSREADY_DB_PASSWORD` | Your DB Password | `your_secure_password` |
| `DATABASE_DRIVER` | PostgreSQL Driver | `org.postgresql.Driver` |

3. Once these secrets are saved, re-trigger your GitHub Actions workflow (`git push origin main`). The app will automatically connect to your persistent PostgreSQL database and run Flyway migrations!
