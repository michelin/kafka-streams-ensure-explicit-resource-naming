# Kafka Streams WITHOUT Explicit Resource Naming (Fails)

This module demonstrates consequences of not naming internal resources in Kafka Streams.

This module **intentionally fails** to start because:  
- `ensure.explicit.internal.resource.naming=true` is enabled
- Internal resources use auto-generated names with numeric indices
- KIP-1111 prevents the application from starting

The error shows exactly which resources need explicit names.

## Prerequisites

To compile and run this demo, you’ll need:

- Java 21
- Maven
- Docker

## Running the Application

To run the application manually:

- Start a [Confluent Platform](https://docs.confluent.io/platform/current/quickstart/ce-docker-quickstart.html#step-1-download-and-start-cp) in a Docker environment.
- Create a topic named `delivery_booked_topic`.
- Create a topic named `item_ref_topic`.
- Create a topic named `delivery_booked_by_item_topic`.

- Start the Kafka Streams application.

To run the application in Docker, use the following command:

```bash
docker-compose up -d
```

This will start the following services in Docker:

- 1 Kafka broker (KRaft mode)
- 1 Control Center
- 1 Kafka Streams Without Naming application

**Expected Error:**
```
Exception in thread "main" org.apache.kafka.streams.errors.TopologyException: Invalid topology:  
Following changelog topic(s) has not been named: item_ref_topic-STATE-STORE-0000000002-changelog, KSTREAM-AGGREGATE-STATE-STORE-0000000011-changelog  
Following state store(s) has not been named: item_ref_topic-STATE-STORE-0000000002, KSTREAM-AGGREGATE-STATE-STORE-0000000011  
Following repartition topic(s) has not been named: KSTREAM-MAP-0000000001-repartition, KSTREAM-AGGREGATE-STATE-STORE-0000000011-repartition  
```

## Next Steps

See the companion module [With Naming](../with-naming) for the corrected version that starts successfully.