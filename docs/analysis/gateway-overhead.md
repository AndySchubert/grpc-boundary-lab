# Gateway Overhead Analysis

Observed behavior from sweep experiments:

- Gateway throughput consistently lower than backend
- Tail latency increases more rapidly under higher concurrency
- Gateway saturates earlier

Example (C=64):

Backend:
- ~18k ok_rps
- p95 ~4–5 ms

Gateway:
- ~14k ok_rps
- p95 ~6–7 ms

This suggests the forwarding boundary introduces measurable scheduling
and queuing overhead under load.
