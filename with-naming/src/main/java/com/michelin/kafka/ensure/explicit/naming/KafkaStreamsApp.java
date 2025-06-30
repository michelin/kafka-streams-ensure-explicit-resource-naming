/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.michelin.kafka.ensure.explicit.naming;

import static org.apache.kafka.streams.StreamsConfig.*;

import com.google.gson.Gson;
import com.michelin.kafka.commons.DeliveryBooked;
import com.michelin.kafka.commons.DeliveryBookedSerde;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;

/** Kafka Streams application. */
public class KafkaStreamsApp {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        final Properties properties = new Properties();
        properties.put(APPLICATION_ID_CONFIG, "ensure-explicit-naming-app");
        properties.put(
                BOOTSTRAP_SERVERS_CONFIG,
                Optional.ofNullable(System.getenv("BOOTSTRAP_SERVERS")).orElse("localhost:19092"));
        properties.put(STATE_DIR_CONFIG, "/tmp/kafka-streams");
        properties.put(ENSURE_EXPLICIT_INTERNAL_RESOURCE_NAMING_CONFIG, true);

        final StreamsBuilder streamsBuilder = new StreamsBuilder(new TopologyConfig(new StreamsConfig(properties)));
        buildTopology(streamsBuilder);
        System.out.println(streamsBuilder.build().describe());
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
        kafkaStreams.start();
    }

    public static void buildTopology(StreamsBuilder streamsBuilder) {

        // Define a custom Serde for DeliveryBooked objects
        DeliveryBookedSerde deliveryBookedSerde = new DeliveryBookedSerde();

        // Define the stream from the delivery booked topic
        // and parse the JSON value into a DeliveryBooked object
        KStream<String, DeliveryBooked> stream = streamsBuilder.stream(
                        "delivery_booked_topic", Consumed.with(Serdes.String(), Serdes.String()))
                //   .filter((k, v) -> true)
                .map((key, value) -> {
                    DeliveryBooked deliveryBooked = parseFromJson(value);
                    return new KeyValue<>(deliveryBooked.getDeliveryId(), deliveryBooked);
                });

        // Define the table from the item reference topic
        KTable<String, String> table = streamsBuilder.table(
                "item_ref_topic",
                Consumed.with(Serdes.String(), Serdes.String()),
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(
                                "ITEM_REF_STORE") // set the store name explicitly
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.String()));

        // Join the stream with the table
        // and enrich the DeliveryBooked object with the item information
        KStream<String, DeliveryBooked> joinedStream = stream.join(
                table,
                (deliveryBooked, item) -> {
                    deliveryBooked.setItem(item);
                    return deliveryBooked;
                },
                Joined.with(Serdes.String(), deliveryBookedSerde, Serdes.String())
                        .withName("JOIN_DELIVERY_BOOKED_ITEM") // set the repartition name explicitly
                );

        // Count the number of deliveries per item
        KStream<String, Long> countStream = joinedStream
                .groupBy(
                        (key, deliveryBooked) -> deliveryBooked.getItem(),
                        Grouped.with(
                                "GROUP_BY_ITEM", // set the repartition name explicitly
                                Serdes.String(),
                                deliveryBookedSerde))
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(
                                "ITEM_COUNT_STORE") // set the store name explicitly
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))
                .toStream();

        // Write the result to the delivery_booked_by_item_topic
        countStream.to("delivery_booked_by_item_topic", Produced.with(Serdes.String(), Serdes.Long()));
    }

    private static DeliveryBooked parseFromJson(String value) {
        return gson.fromJson(value, DeliveryBooked.class);
    }
}
