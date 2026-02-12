package lab.gateway;

public final class GatewayConfig {
    public final int port;
    public final String backendHost;
    public final int backendPort;
    public final int serverThreads;
    public final int clientThreads;

    private GatewayConfig(int port, String backendHost, int backendPort, int serverThreads, int clientThreads) {
        this.port = port;
        this.backendHost = backendHost;
        this.backendPort = backendPort;
        this.serverThreads = serverThreads;
        this.clientThreads = clientThreads;
    }

    public static GatewayConfig fromEnv() {
        return new GatewayConfig(
                Integer.parseInt(System.getenv().getOrDefault("GATEWAY_PORT", "50052")),
                System.getenv().getOrDefault("BACKEND_HOST", "127.0.0.1"),
                Integer.parseInt(System.getenv().getOrDefault("BACKEND_PORT", "50051")),
                Integer.parseInt(System.getenv().getOrDefault("GATEWAY_SERVER_THREADS", "0")),
                Integer.parseInt(System.getenv().getOrDefault("GATEWAY_CLIENT_THREADS", "0"))
        );
    }
}
