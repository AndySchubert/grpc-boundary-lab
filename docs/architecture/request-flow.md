# Request Flow

This page details the journey of a gRPC request through the system.

## Unary Call Path

```mermaid
sequenceDiagram
    participant C as Load Generator
    participant G as Gateway (Async)
    participant B as Backend

    C->>G: gRPC Request (unary)
    Note over G: OnMessage Received
    G->>B: Forward to Backend (Async Stub)
    B-->>G: gRPC Response
    Note over G: OnNext / OnCompleted
    G-->>C: Forward Response to Client
```

## Async Forwarding Logic

The Gateway uses `StreamObserver` to proxy the request. Crucially, it does **not** block a thread waiting for the backend response. Instead:
1. It receives the request.
2. It initiates an asynchronous call to the backend.
3. It provides a callback that forwards the result to the original client when it arrives.

This allows the Gateway to handle thousands of concurrent requests with a small, fixed thread pool.