# Load Generator

The load generator supports the following environment variables:

REQUESTS     (default 50000)   - Measured requests
WARMUP       (default 2000)    - Warmup requests (not measured)
CONCURRENCY  (default 32)      - Worker threads
DEADLINE_MS  (default 20000)   - Per-RPC deadline
RUNS         (default 1)       - Number of repeated measured runs

---

Output Format

run,attempted,ok,errors,concurrency,seconds,ok_rps,p50_us,p95_us,p99_us,max_us

Key Metrics

ok_rps  → successful requests per second
p95_us  → 95th percentile latency
p99_us  → 99th percentile latency
