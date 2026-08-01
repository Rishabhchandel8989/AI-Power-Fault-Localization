## Domain Model

The electrical network is modeled as a hierarchy:

```text
Feeder
   │
   ├── Transformer
           │
           ├── Pole
                   │
                   ├── Device
                           │
                           └── Telemetry
```

This mirrors the physical low-voltage distribution network and allows the localization engine to traverse from feeders to individual telemetry sources.

## Network Model

The electrical distribution network is represented as a hierarchy.

```text
Feeder
   │
   ▼
Transformer
   │
   ▼
Pole
```

Each transformer stores a `topologyKnown` flag indicating whether the downstream pole ordering is available.

This models the assignment requirement that approximately 60% of transformers have incomplete topology information. When topology is unavailable, the localization engine falls back to estimating the affected region instead of claiming an exact span.
```


## Pole Model
The pole is the fundamental unit used by the localization engine.
Each pole belongs to one transformer and may optionally have an IoT device. Approximately 9% of poles intentionally have no device to reflect the assignment's synthetic data requirements.
Pole ordering is represented by the `sequenceNumber` field. When the ordering is unavailable, the field remains `null`, allowing the localization engine to distinguish between known topology and inferred topology.

## Device Layer
IoT devices are installed on poles and act as the source of telemetry data. A pole may not have a device, matching the assignment requirement that approximately 9% of poles are unequipped.
Each device maintains its operational status and last heartbeat (`lastSeen`), enabling the simulator and monitoring dashboard to detect offline devices.

## Incident Model
The system separates electrical faults from operational incidents.
Telemetry events are first processed by the localization engine, which groups multiple dark poles caused by the same electrical fault into a single Incident.
This prevents alert storms where dozens of telemetry events generate dozens of tickets. Each Incident represents one operational problem requiring one crew dispatch and may contain many affected poles.
Tickets are created for Incidents rather than individual telemetry events.

## Electrical Hierarchy

The network is modeled using the same hierarchy described in the assignment.
```text
Substation
    │
    ▼
Feeder
    │
    ▼
Transformer
    │
    ▼
   Pole
```

This structure allows feeder-level and transformer-level outages to be localized independently while maintaining the physical ownership of downstream assets.

## REST API Design

The backend exposes a versioned REST API under `/api/v1`.

### Core APIs

| Module | Purpose |
|---------|---------|
| `/telemetry` | Receive and query IoT telemetry |
| `/incidents` | Fault localization results and lifecycle |
| `/tickets` | Operator ticket workflow |
| `/simulator` | Generate synthetic network and inject faults |
| `/dashboard` | Aggregated metrics for the operator console |
| `/poles` | Pole information and network traversal |
| `/feeders` | Feeder management |
| `/transformers` | Transformer information |
| `/substations` | Substation information |
| `/outages` | Scheduled outage management |
| `/health` | Health check for deployment |

## Localization Engine

The localization engine is implemented as an independent service.

Responsibilities:

- Receive telemetry
- Update latest device state
- Traverse the electrical graph
- Identify the live/dark boundary
- Group affected poles into one incident
- Produce a confidence score
- Return a localization result