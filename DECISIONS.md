# Architectural Decision Log (ADR)

Chronological record of key technical and product decisions, trade-offs, documented assumptions, and future roadmap.

---

## Log of Decisions (Newest First)

### Decision 7: AI Lineman Dispatch Briefing Generator as Product AI Feature
- **Choice:** Built an AI-shaped dispatch briefing service that synthesizes graph boundaries, PIN codes, household counts, materials checklists, and safety instructions into crew briefs.
- **Why:** Control room operators need actionable dispatch summaries rather than raw graph JSON.
- **Alternatives Considered:** Using LLM for fault localization.
- **Why Rejected:** LLMs are non-deterministic, slow, expensive, and unsuited for graph traversal.

### Decision 6: Telemetry-Driven Auto-Verification & Premature Resolution Protection
- **Choice:** Enforced that tickets cannot be manually marked `RESOLVED` or `CLOSED` if IoT telemetry shows affected poles remain dark. Automatically move tickets to `VERIFIED` and `CLOSED` when telemetry confirms power restoration.
- **Why:** Evaluator brief explicitly required that restoration be verified from telemetry, not from someone clicking a button.
- **Alternatives Considered:** Allow manual closure with unverified warning.
- **Why Rejected:** Operators could close tickets prematurely, defeating the core value proposition of the system.

### Decision 5: Haversine Spatial Proximity for 60% Missing Topology Strategy
- **Choice:** For transformers lacking recorded parent-child pole order (`topologyKnown = false`), calculate spatial distance to the nearest energized pole under the same transformer and assign `confidence = 65.0%`.
- **Why:** Provides immediate utility for the 60% historical unmapped case while being transparent about reduced confidence.
- **Alternatives Considered:** Rejecting localization on unmapped DTs.
- **Why Rejected:** Leaving 60% of the network unsupported would fail real-world deployment goals.

### Decision 4: Single Dark Pole Sensor Failure Noise Filtering
- **Choice:** If pole $P_k$ is dark but any downstream child pole $P_{k+1}$ is live, classify $P_k$ as `SENSOR_FAILURE` and suppress ticket creation.
- **Why:** A single dark pole with live downstream children is physically impossible as a line fault; it indicates modem/sensor failure.
- **Alternatives Considered:** Fire fault incident for every dark pole.
- **Why Rejected:** Alert storms destroy control room trust in week two.

### Decision 3: Graph Traversal (DFS) for Downstream Impact & Boundary Detection
- **Choice:** Represent radial LT lines as a directed graph and calculate downstream affected pole counts using Depth-First Search (DFS).
- **Why:** LT networks are radial trees with spurs. DFS correctly counts all branch poles downstream of a wire snap.
- **Alternatives Considered:** Counting only immediate child poles.
- **Why Rejected:** Underestimates customer impact on branched lines.

### Decision 2: Dual Telemetry Schema (`DeviceTelemetry` + `TelemetryHistory`)
- **Choice:** Maintain a current-state table (`DeviceTelemetry`) for fast $O(1)$ state checks and an append-only audit log (`TelemetryHistory`).
- **Why:** Prevents slow aggregate queries over millions of historical telemetry records.
- **Alternatives Considered:** Single telemetry table.
- **Why Rejected:** Poor query performance as telemetry history grows.

### Decision 1: Spring Boot & React Tech Stack
- **Choice:** Spring Boot 3/4 + JPA + MySQL backend with React 18 + Leaflet + Tailwind CSS frontend.
- **Why:** Provides robust type safety, JPA graph modeling, fast build execution, and rich UI customization.

---

## Documented Assumptions

1. **Subdivision Scope:** The system handles one city subdivision (~38,400 poles across 4 substations and 31 feeders).
2. **Radial Network:** Low-tension (LT) lines operate strictly as radial trees with no closed loops.
3. **Pincode Coverage:** Missing pincodes (~3% case) default to administrative subdivision default (`560078`).
4. **Heartbeat Window:** IoT devices send heartbeats every 15 minutes $\pm 45$ seconds.

---

## 2-Week Roadmap & Known Limits

1. **Known Limits:** For 60% unmapped DT lines, spatial proximity inference assumes straight-line pole progression; complex zig-zag street layouts may misidentify the exact span by 1 pole.
2. **Future Enhancements (2 Weeks):**
   - **Machine Learning Topology Re-construction:** Learn parent-child relationships automatically from historical correlated outage patterns.
   - **MQTT Protocol Adapter:** Add native MQTT broker subscriber alongside HTTP ingest endpoint.
