# Gateway Overhead

This page quantifies the "cost of the hop" introduced by the gRPC gateway.

## Latency Amplification

Comparing p50 (median) latency in microseconds (us) at different concurrency levels.

| Concurrency | Backend p50 (us) | Gateway p50 (us) | Delta (us) |
| :--- | :--- | :--- | :--- |
| 1 | 288 | 633 | +345 |
| 16 | 1,051 | 1,296 | +245 |
| 128 | 6,423 | 7,995 | +1,572 |

## Where does the time go?

The ~300µs added by the gateway is a composite of:

1. **Network Latency**  
   Two socket hops instead of one (localhost loopback).

2. **Serialization**  
   The request must be deserialized by the gateway and re-serialized for the backend.

3. **Context Switching**  
   The gateway must hand off the request from the server thread to the client thread (even in the async model).

4. **Queueing**  
   Internal gRPC buffers and event loops.

## Critical Finding: Tail Latency
At high loads, the "Queueing" component becomes dominant. The gateway doesn't just add a fixed offset; it amplifies variance, leading to much larger p99 gaps during saturation.
