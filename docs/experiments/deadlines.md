# Deadlines and Errors

Performance isn't just about speed; it's about reliability under pressure.

## Deadline Exceeded Patterns

During the `sweep.txt` runs, we observed `DEADLINE_EXCEEDED` errors specifically when starting the Gateway tests at low concurrency.

### Error Example:
```
StatusRuntimeException: DEADLINE_EXCEEDED: ClientCall started after CallOptions deadline was exceeded
```

### Analysis:
1. **Warmup Importance**: These errors often occur during the first few requests as JVM classes are loaded and JIT compilation hasn't kicked in.
2. **Cascading Failure**: In a gateway model, if the backend slows down, the gateway's internal queues can fill up. Even if the backend eventually recovers, requests might already be "stale" by the time the gateway tries to forward them.
3. **Configuration**: The lab uses a 20s deadline (`DEADLINE_MS=20000`), which is extremely generous. Errors here indicate either extreme CPU starvation during JVM startup or a misconfiguration in the timing logic of the load generator.

## Recovery
After the initial "hiccups," the system stabilizes and reports 0 errors for the remainder of the 50,000 request run, indicating that the async forwarding logic is robust once "warm."
