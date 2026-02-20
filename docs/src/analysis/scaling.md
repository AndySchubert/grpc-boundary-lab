# Scaling Behavior

This page analyzes how the system scales as concurrency (number of active client threads) increases.

## Throughput (RPS) Scaling

| Concurrency | Backend RPS | Gateway RPS | Throughput Gap |
| :--- | :--- | :--- | :--- |
| 1 | 3,334 | 1,523 | 54% |
| 4 | 11,059 | 5,304 | 52% |
| 16 | 14,709 | 11,607 | 21% |
| 64 | 18,517 | 14,355 | 22% |
| 128 | 19,237 | 15,492 | 19% |

## Observations

1. **Sub-linear Scaling**: Throughput increases with concurrency but reaches a point of diminishing returns (saturation) around concurrency 64-128.
2. **Fixed Overhead Amortization**: At low concurrency (C=1), the gateway's overhead is extreme (halving throughput). However, at high saturation (C=128), the gap narrows to ~19%.
3. **Efficiency**: The asynchronous gateway model scales well, maintaining a consistent relative performance vs the backend even as request rates grow 10x.
