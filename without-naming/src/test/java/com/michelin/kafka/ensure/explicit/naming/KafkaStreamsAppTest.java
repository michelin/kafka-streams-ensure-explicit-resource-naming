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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyConfig;
import org.apache.kafka.streams.errors.TopologyException;
import org.apache.kafka.streams.processor.internals.InternalTopologyBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.junit.jupiter.api.Assertions.*;

class KafkaStreamsAppTest {

    @Test
    void shouldNotCheckAndValidateTheTopology() {

        final Properties properties = new Properties();
        properties.put(APPLICATION_ID_CONFIG, "ensure-explicit-naming-app-app-test");
        properties.put(BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        properties.put(ENSURE_EXPLICIT_INTERNAL_RESOURCE_NAMING_CONFIG, false);

        final StreamsBuilder streamsBuilder = new StreamsBuilder(new TopologyConfig(new StreamsConfig(properties)));
        KafkaStreamsApp.buildTopology(streamsBuilder);

        // Capture logs
        Logger logger = (Logger) LoggerFactory.getLogger(InternalTopologyBuilder.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        // Build the topology without throwing an exception
        assertDoesNotThrow(() -> streamsBuilder.build());

        // Assert: Check logs
        boolean warningLogged = listAppender.list.stream()
                .anyMatch(
                        event -> event.getLevel() == Level.WARN
                                && event.getFormattedMessage()
                                .contains(
                                        """
                                                Explicit naming for internal resources is currently disabled. If you want to enforce user-defined names for all internal resources, set ensure.explicit.internal.resource.naming to true. Note: Changing internal resource names may require a full streams application reset for an already deployed application. Consult the documentation on naming operators for more details. Following changelog topic(s) has not been named: item_ref_topic-STATE-STORE-0000000002-changelog, KSTREAM-AGGREGATE-STATE-STORE-0000000011-changelog
                                                Following state store(s) has not been named: item_ref_topic-STATE-STORE-0000000002, KSTREAM-AGGREGATE-STATE-STORE-0000000011
                                                Following repartition topic(s) has not been named: KSTREAM-MAP-0000000001-repartition, KSTREAM-AGGREGATE-STATE-STORE-0000000011-repartition
                                                """));

        assertTrue(warningLogged);
    }

    @Test
    void shouldCheckAndNotValidateTheTopology() {

        final Properties properties = new Properties();
        properties.put(APPLICATION_ID_CONFIG, "ensure-explicit-naming-app-app-test");
        properties.put(BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        properties.put(ENSURE_EXPLICIT_INTERNAL_RESOURCE_NAMING_CONFIG, true);

        final StreamsBuilder streamsBuilder = new StreamsBuilder(new TopologyConfig(new StreamsConfig(properties)));
        KafkaStreamsApp.buildTopology(streamsBuilder);

        // Build the topology => this should throw a TopologyException
        TopologyException topologyException = assertThrows(TopologyException.class, streamsBuilder::build);

        assertEquals(
                """
                        Invalid topology: Following changelog topic(s) has not been named: item_ref_topic-STATE-STORE-0000000002-changelog, KSTREAM-AGGREGATE-STATE-STORE-0000000011-changelog
                        Following state store(s) has not been named: item_ref_topic-STATE-STORE-0000000002, KSTREAM-AGGREGATE-STATE-STORE-0000000011
                        Following repartition topic(s) has not been named: KSTREAM-MAP-0000000001-repartition, KSTREAM-AGGREGATE-STATE-STORE-0000000011-repartition
                        """,
                topologyException.getMessage());
    }
}
