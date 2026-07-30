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