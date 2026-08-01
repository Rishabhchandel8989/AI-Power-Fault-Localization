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