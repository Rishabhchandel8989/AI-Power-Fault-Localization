# Deployment & Operations Guide — KSPDB System

This guide provides instructions for deploying, verifying, troubleshooting, and resetting the **KSPDB AI Power Fault Localization Platform**.

---

## 1. Prerequisites

- **Docker Desktop:** Version $\ge 24.0.0$ (with Docker Compose v2)
- **RAM:** Minimum 4 GB free memory dedicated to Docker containers.
- **Ports:** 3306 (MySQL), 8080 (Backend API), 5173 (Frontend Operator Console).

---

## 2. One-Command Local Deployment

From a clean clone, run:

```bash
git clone https://github.com/Rishabhchandel8989/AI-Power-Fault-Localization.git
cd AI-Power-Fault-Localization
docker compose up --build
```

### Verification
Once containers start:
1. Open **Operator Console:** [http://localhost:5173](http://localhost:5173).
2. Confirm the map displays green pole markers around Jayanagar/JP Nagar Bangalore.
3. Verify metrics header shows 90 total poles and 6 seeded transformers.

---

## 3. Environment Variables (`.env.example`)

| Variable | Default Value | Required | Description |
| font-mono | font-mono | font-mono | |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql:3306/powerfault?createDatabaseIfNotExist=true` | Yes | Database JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | `root` | Yes | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `root` | Yes | Database password |
| `AI_API_KEY` | *(Optional)* | No | OpenAI/Gemini API key for enhanced LLM briefs. Fallback expert rule generator active when empty. |
| `PORT` | `8080` | No | Backend service port |

---

## 4. Troubleshooting Guide

| Issue / Symptom | Likely Cause | Resolution / Fix |
|-----------------|--------------|------------------|
| `port is already allocated: 3306` | Existing MySQL service running locally. | Stop local MySQL (`net stop MySQL80`) or change port mapping in `docker-compose.yml` to `"3307:3306"`. |
| `port is already allocated: 8080` | Local Java or web service running on 8080. | Kill process on 8080 (`Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess)`) or adjust `docker-compose.yml`. |
| Backend crashes on startup with `Connection Refused` | MySQL container still initializing schemas. | `docker-compose.yml` includes healthchecks. Wait 15 seconds for retry, or run `docker compose restart backend`. |
| Map tiles fail to load | No internet access for Carto/OpenStreetMap tiles. | Ensure network connectivity to `cartocdn.com`. |
| CORS Error on frontend API calls | Direct port access bypass. | The frontend uses Vite proxy for `/api`. Access via `http://localhost:5173`. |

---

## 5. Clean Reset Command

To wipe database volumes and reset to clean seeded state:

```bash
docker compose down -v
docker compose up --build
```
