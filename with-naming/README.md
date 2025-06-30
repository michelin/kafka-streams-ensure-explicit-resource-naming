# Kafka Streams WITH Explicit Resource Naming Demo (Working Example)

This module demonstrates a Kafka Streams application with **explicit naming** for all internal resources (state stores, repartition topics).  

This is the **recommended approach** for production applications.

## What to Observe

When running this application, you'll notice:
- State store names are explicit: `ITEM_REF_STORE`, `ITEM_COUNT_STORE`
- Changelog and Repartition topics have meaningful names: `ITEM_REF_STORE-changelog`, `ITEM_COUNT_STORE-changelog`, `JOIN_DELIVERY_BOOKED_ITEM-repartition`, `GROUP_BY_ITEM-repartition`
- No auto-generated numeric indices in topic names
- Topology changes won't break existing state

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
- 1 Kafka Streams With Naming application

**The Kafka Streams application is running fine**