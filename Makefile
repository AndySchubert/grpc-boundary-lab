.PHONY: help build clean backend gateway loadgen loadgen-2k loadgen-gw loadgen-backend

# ---- Loadgen defaults ----
REQUESTS ?= 50000
WARMUP ?= 2000
CONCURRENCY ?= 32
DEADLINE_MS ?= 20000
RUNS ?= 1
TARGET_HOST ?= 127.0.0.1

# ---- Server defaults ----
BACKEND_PORT ?= 50051
GATEWAY_PORT ?= 50052

define RUN_LOADGEN
	WARMUP=$(WARMUP) \
	CONCURRENCY=$(CONCURRENCY) \
	DEADLINE_MS=$(DEADLINE_MS) \
	RUNS=$(RUNS) \
	REQUESTS=$(REQUESTS) \
	TARGET_HOST=$(TARGET_HOST) \
	TARGET_PORT=$(1) \
	./gradlew :loadgen:run
endef

help:
	@echo "Targets:"
	@echo "  build           - build all modules"
	@echo "  clean           - clean build outputs"
	@echo "  backend         - run backend server (CTRL+C to stop)"
	@echo "  gateway         - run gateway server (CTRL+C to stop)"
	@echo "  loadgen-backend - run loadgen against backend (port $(BACKEND_PORT))"
	@echo "  loadgen-gw      - run loadgen against gateway (port $(GATEWAY_PORT))"
	@echo "  loadgen         - alias for loadgen-backend"
	@echo "  loadgen-2k      - run 2000 requests against backend"
	@echo ""
	@echo "Loadgen vars (override like: make loadgen-gw CONCURRENCY=64 RUNS=5):"
	@echo "  TARGET_HOST=$(TARGET_HOST)"
	@echo "  REQUESTS=$(REQUESTS) WARMUP=$(WARMUP) CONCURRENCY=$(CONCURRENCY) DEADLINE_MS=$(DEADLINE_MS) RUNS=$(RUNS)"

build:
	./gradlew build

clean:
	./gradlew clean

backend:
	BACKEND_PORT=$(BACKEND_PORT) ./gradlew :backend:run

gateway:
	GATEWAY_PORT=$(GATEWAY_PORT) BACKEND_HOST=$(TARGET_HOST) BACKEND_PORT=$(BACKEND_PORT) ./gradlew :gateway:run

loadgen: loadgen-backend

loadgen-backend:
	$(call RUN_LOADGEN,$(BACKEND_PORT))

loadgen-gw:
	$(call RUN_LOADGEN,$(GATEWAY_PORT))

loadgen-2k:
	$(MAKE) loadgen-backend REQUESTS=2000

.PHONY: sweep

# Space-separated list of concurrencies to test
SWEEP_CONC ?= 1 4 16 32 64 128

sweep:
	@echo "sweep: REQUESTS=$(REQUESTS) WARMUP=$(WARMUP) DEADLINE_MS=$(DEADLINE_MS) RUNS=$(RUNS) TARGET_HOST=$(TARGET_HOST)"
	@for c in $(SWEEP_CONC); do \
	  echo ""; \
	  echo "==================== BACKEND  CONCURRENCY=$$c ===================="; \
	  $(MAKE) --no-print-directory loadgen-backend CONCURRENCY=$$c; \
	  echo "==================== GATEWAY   CONCURRENCY=$$c ===================="; \
	  $(MAKE) --no-print-directory loadgen-gw CONCURRENCY=$$c; \
	done
