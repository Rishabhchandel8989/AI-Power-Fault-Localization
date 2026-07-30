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