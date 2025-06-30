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
package com.michelin.kafka.ensure.explicit;

import com.michelin.kafka.ensure.explicit.naming.KafkaStreamsApp;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.Test;

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

        // Build the topology => this should throw a TopologyException
        assertDoesNotThrow(() -> streamsBuilder.build());

    }

    @Test
    void shouldCheckAndValidateTheTopology() {

        final Properties properties = new Properties();
        properties.put(APPLICATION_ID_CONFIG, "ensure-explicit-naming-app-app-test");
        properties.put(BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        properties.put(ENSURE_EXPLICIT_INTERNAL_RESOURCE_NAMING_CONFIG, true);

        final StreamsBuilder streamsBuilder = new StreamsBuilder(new TopologyConfig(new StreamsConfig(properties)));
        KafkaStreamsApp.buildTopology(streamsBuilder);

        // Build the topology => this should throw a TopologyException
        assertDoesNotThrow(() -> streamsBuilder.build());
    }

}
