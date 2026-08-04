# AI Workflow & Engineering Leverage Report

This document records how AI tools were utilized during the design and development of the **KSPDB AI Power Fault Localization Platform**, what tasks were delegated versus human-owned, concrete AI mistakes encountered, and prompt excerpts.

---

## 1. AI Tools Employed

- **ChatGPT (GPT-4o):** Domain exploration, initial entity structure brainstorming, and document drafting.
- **Cursor / Claude 3.5 Sonnet / Antigravity IDE (Gemini 3.6 Flash):** Codebase scaffolding, graph traversal implementation, React Tailwind UI component generation, and unit test generation.

---

## 2. Delegation & Code Ownership Matrix

| Task Category | AI Delegated vs. Human-Written | Rationale |
| font-mono | font-mono | |
| **Domain Schema & Entities** | 40% AI / 60% Human | AI generated initial JPA annotations; Human structured `Substation` $\to$ `Feeder` $\to$ `Transformer` $\to$ `Pole` $\to$ `Device` hierarchy and dual telemetry tables. |
| **Core Fault Localization Engine** | 20% AI / 80% Human | The boundary detection graph algorithm and 60% Haversine missing topology fallback were designed and written by Human to guarantee mathematical determinism. |
| **Telemetry Ingestion & De-duplication** | 30% AI / 70% Human | Human designed the monotonic `seq` deduplication and silent FW 1.2 quiet sensor detection rules. |
| **Auto-Verified Ticket Lifecycle** | 30% AI / 70% Human | Human wrote the strict telemetry verification check (`isIncidentRestoredFromTelemetry`) rejecting premature manual resolution. |
| **React Leaflet Dashboard & UI** | 70% AI / 30% Human | AI generated modern Tailwind layout, Leaflet layer integration, and incident feed styling; Human tuned state polling and action error banners. |
| **Documentation & ADRs** | 50% AI / 50% Human | AI assisted with text formatting and Markdown templates; Human provided technical rationale, metrics, and failure modes. |

---

## 3. Concrete AI Mistakes & Misleading Code

### Mistake 1: Initial Recommendation to Use an LLM for Fault Localization
- **What the AI Suggested:** Early ChatGPT prompts suggested passing incoming telemetry payloads to an LLM prompt (e.g. GPT-4) to "reason about which pole failed".
- **Why it was Wrong:** Graph traversal on 38,400 poles requires deterministic $< 2\text{ ms}$ processing. An LLM call is non-deterministic, takes $1\text{--}3\text{ seconds}$, costs money per message, and hallucinates on spatial topology graphs.
- **How it was Fixed:** Rejected LLM for localization; built a graph algorithm instead. Utilized AI for synthesizing operator dispatch briefings (`AiBriefingService`) where natural language synthesis adds genuine value.

### Mistake 2: Single Table Telemetry Query Overhead
- **What the AI Suggested:** AI generated a single `Telemetry` entity for all historical and real-time events.
- **Why it was Wrong:** Finding the latest state of 34,900 devices required `GROUP BY device_id` over millions of historical records, causing high DB load.
- **How it was Fixed:** Split into dual tables: `DeviceTelemetry` (1 row per device current state) and `TelemetryHistory` (append-only audit log).

---

## 4. Prompts & Session Excerpts

### Prompt Example: AI Lineman Dispatch Briefing Service
> *"Design a Spring Boot service that takes a localized fault incident (span, GPS, pincode, confidence, affected household count) and generates a structured dispatch briefing for linemen including required repair materials, isolation safety directives, and topology notes."*

### Code Generation Summary
- **Total Repository Code:** ~3,800 lines of Java and JSX.
- **Estimated AI-Generated Share:** ~65% (primarily DTOs, boilerplate controllers, React UI styling, and documentation templates).
- **Human-Verified Share:** 100% of graph traversal logic, test assertions, and data integrity constraints were inspected and verified.
