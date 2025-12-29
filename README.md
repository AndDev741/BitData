# BitData

Reactive Spring Boot/WebFlux pipeline that ingests unconfirmed blockchain transactions, enriches them, and persists analytics with Kafka, MongoDB, and full Prometheus/Grafana observability.

## Architecture (at a glance)
- **Ingress**: WebSocket connector pulls unconfirmed blockchain transactions.
- **Reactive core**: WebFlux handlers + Reactor pipelines orchestrate validation, enrichment, and dispatch.
- **Messaging**: Spring Cloud Stream (Kafka binder) with two queues:
  - `processTransactions`: raw/unconfirmed transactions for primary processing.
  - `advancedProcess`: enriched analytics and advanced statistics.
- **Services**:
  - **Unconfirmed Transaction Service**: validates, deduplicates, publishes to `processTransactions`.
  - **Analytics Service**: computes stats and persists to MongoDB; produces to `advancedProcess` for heavier processing.
  - **Advanced Process Service**: downstream enrichment and persistence of advanced metrics.
- **Persistence**: Reactive MongoDB for raw transactions + computed statistics.
- **External dependency**: Wallet mock API (HTTP client) for downstream lookups.
- **Observability**: Actuator + Micrometer Prometheus registry feeding a provisioned Grafana dashboard (JVM, HTTP, Netty, Kafka, and custom counters).

> See `compose.yaml` for the full runtime: app, Kafka+Zookeeper, MongoDB, wallet-mock, Prometheus, Grafana (with provisioning).

## Data flow
1. WebSocket connector receives unconfirmed transactions from the blockchain feed.
2. Unconfirmed Transaction Service validates and publishes to `processTransactions` (Kafka).
3. Analytics Service consumes `processTransactions`, computes stats, persists to MongoDB, and emits analytics to `advancedProcess`.
4. Advanced Process Service consumes `advancedProcess`, performs heavier enrichment, and persists advanced statistics.
5. Wallet API client is used where external context is required.

## Key components
- **WebFlux + Netty**: Reactive HTTP stack (7070).
- **Kafka (Spring Cloud Stream)**:
  - Producer bindings: `sendUnconfirmedTransactions`, `sendAnalyticsToAdvancedProcess`
  - Consumer bindings: `processTransactions`, `advancedProcess`
- **Reactive MongoDB**: Primary store for raw and derived statistics.
- **Wallet mock**: Local HTTP dependency in `mock-wallet-service`.
- **Observability**:
  - Actuator `/actuator/prometheus` (Prometheus scrape target).
  - Custom Micrometer counters in `CustomMetrics.java` (unconfirmed/failed/retries/raw saves/statistics/advanced stats).
  - Netty metrics enabled via `NettyMetricsConfig`.
  - HTTP histograms enabled (`management.metrics.distribution.percentiles-histogram.http.server.requests=true`).
  - Grafana provisioning under `grafana/provisioning/*` with a ready-made dashboard.

## Metrics to watch
- **Custom counters**: `bitdata_ws_unconfirmed_transactions_total`, `bitdata_ws_failed_transactions_total`, `bitdata_ws_retries_total`, `bitdata_raw_transactions_save_total`, `bitdata_statistics_persisted_total`, `bitdata_advanced_statistics_persisted_total`, `bitdata_advanced_statistics_failed_total`.
- **HTTP**: `http_server_requests_seconds_*` (rate, p50, p95), error rate, active requests.
- **JVM**: `jvm_memory_*`, `jvm_threads_*`, `process_cpu_usage`, uptime.
- **Netty**: `reactor_netty_http_server_response_time_seconds_*`, buffer allocators (`reactor_netty_bytebuf_allocator_*`), connections.
- **Kafka**:
  - Backlog: `spring_cloud_stream_binder_kafka_offset{topic,group}`
  - Lag: `kafka_consumer_fetch_manager_records_lag{topic}`
  - Consume rate: `kafka_consumer_fetch_manager_records_consumed_rate{topic}`
  - Produce rate: `kafka_producer_topic_record_send_rate{topic}`

## Running locally
```sh
# build the app image (or use Build.sh)
./mvnw clean package -DskipTests

# start the stack
docker compose up -d --build

# Grafana: http://localhost:3003 (admin/admin)
# Prometheus: http://localhost:9095
# App: http://localhost:7070 (Actuator metrics at /actuator/prometheus)
```

## Development notes
- Java 23, Spring Boot 3.3.x, Spring Cloud 2023.0.x, WebFlux, Reactor, Spring Cloud Stream Kafka, Reactive MongoDB.
- Metrics are exposed by default; no extra flag needed beyond included config.
- Grafana is auto-provisioned (datasource + dashboard) via `grafana/provisioning/*` mounts in `compose.yaml`.

## Next steps (ideas)
- Add OpenTelemetry tracing to follow a transaction across HTTP → Kafka → Mongo.
- Introduce DLQ/parking-lot topics for failed messages and surface them in Grafana.
- Add load/chaos experiments to observe backpressure and consumer lag behavior.
