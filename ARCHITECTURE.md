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

## Fault Localization Algorithm

The localization engine follows a deterministic workflow:

1. Process incoming telemetry.
2. Update the latest device state.
3. Ignore duplicate or out-of-order telemetry using the device sequence number.
4. Locate the affected pole.
5. Identify the last energized upstream pole using known topology.
6. Infer the fault span between the last energized pole and the first de-energized pole.
7. Estimate downstream impact by traversing the network.
8. Compute a confidence score based on topology completeness.
9. Create one Incident.
10. Create one Ticket.

## Fault Localization Engine

The localization engine models the LT electrical network as a directed graph.

### Known topology

For transformers where parent pole information exists:

1. Traverse the graph from the first de-energized pole.
2. Identify the upstream energized boundary.
3. Count all downstream poles using Depth-First Search (DFS).
4. Produce one Incident.

### Missing topology

For transformers without recorded parent relationships:

1. Collect poles belonging to the same transformer.
2. Estimate the fault boundary using GPS proximity.
3. Return an estimated span with reduced confidence.

This hybrid strategy allows the system to provide useful localization even when historical network topology is incomplete.

## Graph Traversal

The localization engine models the electrical network as a directed graph using the `NetworkConnection` entity.

Downstream impact is calculated using a Depth-First Search (DFS) traversal starting from the first de-energized pole. This allows all affected poles, including poles on branches, to be counted as part of the same incident.
## Ticket Workflow

Each detected fault automatically creates one ticket.

Ticket lifecycle:

Detected → Acknowledged → Crew Assigned → Resolved → Verified → Closed

The **Verified** state is driven by telemetry, not by operator input. A ticket is not verified until power restoration is confirmed through incoming telemetry from the affected poles.

## Fault Simulator

The backend contains a dedicated simulator module that generates synthetic telemetry matching the assignment's device contract.

Supported scenarios:

- Span fault
- Distribution transformer fault
- Feeder fault
- Device failure
- Fault repair

The simulator is exposed through REST endpoints and will later be integrated with the operator dashboard to allow reviewers to inject faults interactively.

## Fault Localization Workflow

The localization engine follows a deterministic workflow:

1. Store incoming telemetry.
2. Identify the affected pole.
3. Determine the upstream energized pole when topology is available.
4. Infer the fault span.
5. Estimate downstream impact.
6. Create one Incident.
7. Automatically create one Ticket.
8. Return a structured localization response.

If topology is unavailable, the system degrades gracefully by returning an estimated location with reduced confidence instead of failing.

## Fault Localization Flow

1. Telemetry is received from a pole.
2. The telemetry is stored.
3. The reported pole is treated as the first dark pole.
4. The parent pole is checked to identify the last energized pole.
5. A graph traversal discovers all downstream affected poles.
6. Confidence is calculated based on topology completeness.
7. An Incident is created.
8. A Ticket is automatically generated.

## Graph Traversal

The backend models the LT distribution network as a directed graph.

Each pole represents a node.

Each electrical connection represents an edge.

When a pole loses power, the localization engine performs a graph traversal starting from the first de-energized pole to determine every downstream affected pole.

This guarantees that a single span fault produces one Incident instead of one alert per pole.
## Simulator Architecture

The simulator is implemented as a dedicated module.

A single REST endpoint accepts simulation requests and dispatches them to specialized simulation methods based on the requested fault type.

This design separates the external API from the internal simulation logic and allows new fault types to be added without changing the API contract.

## Decision 16

### Choice

Designed the simulator with a single API endpoint and internal dispatching.

### Why

This keeps the public API stable while allowing new simulation scenarios to be added with minimal changes.

### Alternative

Create one endpoint per fault type.

### Why Rejected

It increases API surface area and duplicates controller logic.
