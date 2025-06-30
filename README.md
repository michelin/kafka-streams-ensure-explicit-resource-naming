# Ensure Kafka Streams Explicit Resource Naming

[![GitHub Build](https://img.shields.io/github/actions/workflow/status/michelin/kafka-streams-ensure-explicit-resource-naming/build.yml?branch=main&logo=github&style=for-the-badge)](https://github.com/michelin/kafka-streams-ensure-explicit-resource-naming/actions/workflows/build.yml)
[![Kafka Version](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fmichelin%2Fkafka-streams-ensure-explicit-resource-naming%2Fmain%2Fpom.xml&query=%2F*%5Blocal-name()%3D'project'%5D%2F*%5Blocal-name()%3D'properties'%5D%2F*%5Blocal-name()%3D'kafka-streams.version'%5D%2Ftext()&style=for-the-badge&logo=apachekafka&label=version)](https://github.com/michelin/kafka-streams-ensure-explicit-resource-naming/blob/main/pom.xml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?logo=apache&style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)

[Overview](#-Overview) • [The Problem](#-the-problem) • [why-this-is-a-problem](#why-this-is-a-problem) • [The Solution with KIP-1111](#-the-solution-kip-1111) • [Important Warning](#-important-warning) • [Examples](#examples)

Available since Apache Kafka 4.1.0, A practical demonstration of **KIP-1111: Enforcing Explicit Naming for Kafka Streams Internal Topics** - showcasing why explicit naming of internal resources is crucial for production-ready Kafka Streams applications.

## 🎯 Overview

This project demonstrates the implementation and benefits of [KIP-1111](https://cwiki.apache.org/confluence/display/KAFKA/KIP-1111:+Enforcing+Explicit+Naming+for+Kafka+Streams+Internal+Topics), which introduces the `ensure.explicit.internal.resource.naming` configuration to enforce explicit naming of Kafka Streams internal topics and state stores.

## 🔍 The Problem

Kafka Streams automatically generates names for internal topics (changelog and repartition topics) and state stores when not explicitly specified. These auto-generated names include numeric indices based on topology position:

```
my-app-KSTREAM-KEY-SELECT-0000000003-repartition
my-app-KSTREAM-AGGREGATE-STATE-STORE-0000000007-changelog
```
## Why This Is a Problem

- **Instability during updates**: Any change in topology can alter the processor order, thus modifying the index and internal topic names.
- **State loss**: A changed changelog topic name causes complete state loss.
- **Maintenance complexity**: Auto-generated names make debugging and monitoring harder.

## 🔍 Real-World Example

### Initial Topology:

```java
builder.stream("clicks")
    .groupByKey()
    .count()  // Store: KSTREAM-AGGREGATE-STATE-STORE-0000000001
    .toStream()
    .to("total-clicks");
```

### Adding a filter

```java
builder.stream("clicks")
    .filter((k, v) -> isValid(v))  // New processor !
    .groupByKey()
    .count()  // Store: KSTREAM-AGGREGATE-STATE-STORE-0000000002 ⚠️
    .toStream()
    .to("total-clicks");
```

The store name changes from `0000000001` to `0000000002`, resulting in full state loss!

## 🚀 The Solution: KIP-1111

KIP-1111 introduces a new configuration that prevents applications from starting if any internal topics use auto-generated names:

```java
properties.put(ENSURE_EXPLICIT_INTERNAL_RESOURCE_NAMING_CONFIG, true);
```

## ⚠️ Important Warning

### Configuration Behavior

When `ensure.explicit.internal.resource.naming` is set to:

- **`false` (default)**: The application will **only show warnings** in logs for resources with auto-generated names, but will start normally.
- **`true`**: The application will **fail to start** with a `TopologyException` if any internal resources have auto-generated names.

### For Existing Applications

> **🚨 CRITICAL**: If you have an existing Kafka Streams application and want to enable `ensure.explicit.internal.resource.naming=true`, you **MUST** handle data migration manually.

**You are responsible for:**
- Migrating existing **changelog topic data** to new explicitly-named topics
- Migrating **local state store data** or accepting state rebuild from changelogs
- Ensuring **no data loss** during the transition

**Failure to migrate data properly will result in:**
- ❌ Loss of application state
- ❌ Need to reprocess all historical data
- ❌ Potential business impact

## 🔧 Code Examples

### Before: Auto-Generated Names (Fragile)

```java
// Implicit naming - fragile to topology changes
KTable table = streamsBuilder.table(
    "item_ref_topic",
    Consumed.with(Serdes.String(), Serdes.String()),
    Materialized.with(Serdes.String(), Serdes.String()) // Auto-generated store name
);

KStream joinedStream = stream.join(
    table,
    (deliveryBooked, item) -> {
        deliveryBooked.setItem(item);
        return deliveryBooked;
    },
    Joined.with(Serdes.String(), deliveryBookedSerde, Serdes.String()) // Auto-generated repartition topic
);
```

### After: Explicit Names (Robust)

```java
// Explicit naming - topology-change resistant
KTable table = streamsBuilder.table(
    "item_ref_topic",
    Consumed.with(Serdes.String(), Serdes.String()),
    Materialized
        .<String, String, KeyValueStore<Bytes, byte[]>>as("ITEM_REF_STORE")  // ✅ Explicit store name
        .withKeySerde(Serdes.String())
        .withValueSerde(Serdes.String())
);

KStream joinedStream = stream.join(
    table,
    (deliveryBooked, item) -> {
        deliveryBooked.setItem(item);
        return deliveryBooked;
    },
    Joined.with(Serdes.String(), deliveryBookedSerde, Serdes.String())
        .withName("JOIN_DELIVERY_BOOKED_ITEM")  // ✅ Explicit repartition topic name
);
```

## Examples

This project demonstrates KIP-1111 through two modules:

- [Without Naming](/without-naming) - Shows what happens when explicit naming is NOT used ❌
- [With Naming](/with-naming) - Shows the correct approach with explicit naming ✅
