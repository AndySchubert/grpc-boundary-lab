.PHONY: help build clean backend gateway loadgen loadgen-2k loadgen-gw loadgen-backend


help:
	@echo "Targets:"
	@echo "  build        - build all modules"
	@echo "  clean        - clean build outputs"
	@echo "  backend      - run backend server (CTRL+C to stop)"
	@echo "  loadgen      - run load generator (REQUESTS=200 default)"
	@echo "  loadgen-2k   - run load generator with 2000 requests"
	@echo ""
	@echo "Env vars:"
	@echo "  BACKEND_HOST (default 127.0.0.1)"
	@echo "  BACKEND_PORT (default 50051)"
	@echo "  REQUESTS     (default 200)"

build:
	./gradlew build

clean:
	./gradlew clean

backend:
	./gradlew :backend:run

loadgen:
	REQUESTS=$${REQUESTS:-200} BACKEND_HOST=$${BACKEND_HOST:-127.0.0.1} BACKEND_PORT=$${BACKEND_PORT:-50051} \
	./gradlew :loadgen:run

loadgen-2k:
	REQUESTS=2000 BACKEND_HOST=$${BACKEND_HOST:-127.0.0.1} BACKEND_PORT=$${BACKEND_PORT:-50051} \
	./gradlew :loadgen:run

gateway:
	GATEWAY_PORT=$${GATEWAY_PORT:-50052} BACKEND_HOST=$${BACKEND_HOST:-127.0.0.1} BACKEND_PORT=$${BACKEND_PORT:-50051} \
	./gradlew :gateway:run

loadgen-gw:
	REQUESTS=$${REQUESTS:-200} TARGET_HOST=$${TARGET_HOST:-127.0.0.1} TARGET_PORT=$${TARGET_PORT:-50052} \
	./gradlew :loadgen:run

loadgen-backend:
	REQUESTS=$${REQUESTS:-200} TARGET_HOST=$${TARGET_HOST:-127.0.0.1} TARGET_PORT=$${TARGET_PORT:-50051} \
	./gradlew :loadgen:run
