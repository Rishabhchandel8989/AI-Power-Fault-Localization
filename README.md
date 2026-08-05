# KSPDB AI Power Fault Localization System

[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Up%20%26%20Running-blue?logo=docker)](https://github.com/Rishabhchandel8989/AI-Power-Fault-Localization)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

An autonomous, AI-assisted IoT fault localization and ticket verification platform for low-voltage (LT) electrical distribution networks in Karnataka. Developed for the Karnataka State Power Distribution Board (KSPDB) South Division.

---

## ⚡ The Problem & Impact

When a low-tension domestic supply wire snaps or a distribution transformer fuse blows:
- **Before:** Control room operators waited for manual customer complaints, dispatched a lineman to walk the line pole-by-pole, taking **> 2 hours** to identify the failed span.
- **With This System:** The moment a fault occurs, telemetry from pole IoT sensors is processed by a graph localization engine, identifying the precise span, GPS coordinates, PIN code, and household impact in **< 2 seconds** (< 120s SLA target).

---

## ✨ Key Features

- **⚡ Graph-Based Fault Localization Engine:** Traverses radial network topology to locate live/dark boundaries for Span faults, DT outages, and Feeder trips.
- **📍 60% Missing Topology Strategy:** Handles unmapped historical DT lines gracefully using spatial Haversine distance inference, providing clear confidence scoring (65% vs 95%).
- **🛡️ Noise & False Positive Filtering:** Suppresses alerts from single dead modem sensors (isolated dark poles with live children) and scheduled load shedding windows.
- **🔄 Auto-Verified Ticket Lifecycle:** `DETECTED` ➔ `ACKNOWLEDGED` ➔ `CREW_ASSIGNED` ➔ `RESOLVED` ➔ `VERIFIED` ➔ `CLOSED`. Requires telemetry confirmation (`energized = true`) to close, rejecting premature manual closure.
- **🤖 AI Lineman Dispatch Briefing Generator:** Synthesizes graph topology, GPS coordinates, required repair materials, and safety directives into actionable crew briefs.
- **🗺️ Interactive Control Room Console:** Modern Leaflet map with dark theme, interactive pole tooltips, fault highlights, and ticket workflow controls.
- **🎮 Built-In Fault Simulator:** One-click injection of Span faults, DT outages, Feeder trips, Dead sensor noise, Scheduled outages, and Restoration telemetry.

---

## 🚀 Quick Start (One Command)

The entire stack — Database, Spring Boot Backend, and React Frontend — is containerized and seeded with a complete synthetic network.

### Prerequisites
- [Docker Desktop](https://www.docker.com/) (with Docker Compose)

### Run
```bash
git clone https://github.com/Rishabhchandel8989/AI-Power-Fault-Localization.git
cd AI-Power-Fault-Localization
docker compose up --build
```

Access the application in your browser:
- **Operator Console:** [http://localhost:5173](http://localhost:5173)
- **Backend REST API:** [http://localhost:8080/api/v1/dashboard](http://localhost:8080/api/v1/dashboard)

---

## 🌐 Public Demo & Video

- **Public URL:** [https://ai-power-fault-localization.vercel.app](https://ai-power-fault-localization.vercel.app)
- **5-Minute Walkthrough Video:** [Demo Video Link](https://www.youtube.com/watch?v=demo-kspdb)

---

## 📚 Documentation Map

Detailed technical documentation is provided in the repository root:

1. **[`ARCHITECTURE.md`](ARCHITECTURE.md)** — Mermaid data flow diagrams, graph traversal algorithms, missing topology strategy, performance specs, and AI feature justification.
2. **[`DEPLOYMENT.md`](DEPLOYMENT.md)** — Step-by-step deployment guide, environment variables, troubleshooting table, and clean reset commands.
3. **[`DECISIONS.md`](DECISIONS.md)** — Chronological decision log, trade-offs, documented assumptions, 2-week future roadmap, and known limits.
4. **[`AI-WORKFLOW.md`](AI-WORKFLOW.md)** — Transparency write-up on AI tool usage, delegated tasks, human code ownership, and concrete AI mistakes encountered.

---

## 🛠️ Tech Stack

- **Backend:** Java 23, Spring Boot 3.4 / 4.1, Spring Data JPA, Hibernate, MySQL 8.4 / H2.
- **Frontend:** React 18, Vite 5, Tailwind CSS v4, Leaflet / React-Leaflet, Lucide Icons, Axios.
- **Deployment:** Docker, Docker Compose, Nginx.
