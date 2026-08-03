## Decision 1

### Choice
Use Spring Boot instead of Node.js.

### Reason

- Better suited for enterprise applications.
- Strong JPA support.
- Familiar ecosystem.

### Alternatives

- Node.js
- FastAPI

### Status

Accepted


## Decision 5

### Choice

Split telemetry into two tables.

- DeviceTelemetry
- TelemetryHistory

### Why

The operator dashboard only requires the latest telemetry for each device. Querying millions of historical records would be inefficient.

Keeping a dedicated current-state table allows fast fault localization while preserving complete history for replay, debugging and simulation.

### Alternative

A single telemetry table.

### Why Rejected

It would require expensive queries to determine the latest state for every device as telemetry volume grows.

## Decision 6

### Choice

Replace the Fault entity with an Incident entity.

### Why

The assignment requires grouping many telemetry events into a single operational event. An Incident represents the operator's view of an outage, while a physical fault is the underlying electrical cause.

This separation allows one snapped wire affecting dozens of poles to produce exactly one incident and one ticket.

### Alternative

Model every fault as a separate database record.

### Why Rejected

It encourages one alert per dark pole and makes grouping more complex. An Incident-first model more closely matches real Outage Management Systems (OMS).
## Decision 7

### Choice

Added a Substation entity as the root of the electrical hierarchy.

### Why

Although localization primarily occurs between poles, feeder-level outages originate from substations. Modeling the complete hierarchy improves the realism of the network representation and simplifies feeder-level outage handling.

### Alternative

Start the model at Feeder.

### Why Rejected

It omits an important level of the electrical network described in the assignment and makes feeder ownership less explicit.

## Decision 8

### Choice

Implemented a deterministic graph-based localization engine instead of using an LLM.

### Why

Fault localization is a graph traversal problem with deterministic rules based on electrical topology. A graph algorithm is faster, explainable, free to run, and produces reproducible results.

### Alternative

Use an LLM to infer the fault location.

### Why Rejected

An LLM cannot guarantee deterministic, explainable localization and is unnecessary for a problem that is fundamentally graph traversal.

## Decision 9

### Choice

Used graph traversal (DFS) for downstream impact estimation.

### Why

The electrical network is radial. DFS naturally traverses all downstream poles, including branches, allowing accurate estimation of outage impact.

### Alternative

Count only immediate child poles.

### Why Rejected

Immediate child counting underestimates outages on branched LT networks and fails to represent the actual customer impact.

## Decision 9

### Choice

Used Depth-First Search (DFS) to calculate downstream impact.

### Why

The low-tension distribution network is radial with possible branches. DFS traverses all downstream poles, providing an accurate estimate of outage impact.

### Alternative

Count only immediate child poles.

### Why Rejected

It underestimates the number of affected poles on branched networks and does not match the physical behavior of the electrical network.

## Decision 11

### Choice

Implemented a state-based ticket lifecycle.

### Why

Separating ticket states reflects the real operational workflow of an electricity distribution control room. Automatic verification prevents operators from closing incidents before restoration is confirmed.

### Alternative

Allow manual closure immediately after a crew marks the fault as resolved.

### Why Rejected

The assignment explicitly requires restoration to be verified from telemetry rather than operator actions.

## Decision 12

### Choice

Implemented a dedicated simulator module rather than embedding simulation logic into controllers or services.

### Why

Separating simulation from production logic keeps the localization engine deterministic while providing a repeatable way to generate realistic telemetry for testing and demonstrations.

### Alternative

Generate mock telemetry directly inside controllers.

### Why Rejected

It tightly couples testing logic with application logic and makes the simulator difficult to maintain and extend.

## Decision 13

### Choice

Modeled Incident and Ticket as separate domain entities.

### Why

An Incident represents a physical electrical fault, while a Ticket represents the operational workflow used by the control room.

This separation allows multiple workflow states to exist independently of the fault itself and matches the assignment's required lifecycle.

### Alternative

Store workflow information directly inside Incident.

### Why Rejected

It mixes operational workflow with fault localization, making the domain model harder to extend and maintain.
## Decision 14

### Choice

Implemented graceful degradation for missing network topology.

### Why

Approximately 60% of transformers do not have recorded parent pole information. Instead of failing localization, the system reports an estimated fault location with lower confidence.

### Alternative

Reject localization requests when topology is incomplete.

### Why Rejected

This would leave the majority of the network unsupported and does not meet the assignment requirements.

## 2026-08-03

### Decision
Use deterministic graph traversal for fault localization.

### Alternatives Considered
- LLM-based reasoning
- Rule engine

### Why
Graph traversal is deterministic, explainable, fast, and directly matches the evaluation criteria. It also handles downstream fault grouping better than treating each dark pole as an independent fault.

## Decision 15

### Choice

Represented the electrical network as a directed graph.

### Why

Graph traversal allows efficient downstream fault propagation, incident grouping, and future extensions such as branch analysis and multiple simultaneous outages.

### Alternative

Recursive database queries or adjacency stored inside each entity.

### Why Rejected

Graph traversal is simpler, deterministic, and scales better with the assignment's topology.
