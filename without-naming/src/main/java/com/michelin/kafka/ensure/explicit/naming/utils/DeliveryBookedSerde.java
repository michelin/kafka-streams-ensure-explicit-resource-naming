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
package com.michelin.kafka.ensure.explicit.naming.utils;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class DeliveryBookedSerde implements Serde<DeliveryBooked> {

    private final Gson gson = new Gson();

    @Override
    public Serializer<DeliveryBooked> serializer() {
        return new Serializer<DeliveryBooked>() {
            @Override
            public byte[] serialize(String topic, DeliveryBooked data) {
                if (data == null) return null;
                return gson.toJson(data).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {}

            @Override
            public void close() {}
        };
    }

    @Override
    public Deserializer<DeliveryBooked> deserializer() {
        return new Deserializer<DeliveryBooked>() {
            @Override
            public DeliveryBooked deserialize(String topic, byte[] data) {
                if (data == null || data.length == 0) return null;
                return gson.fromJson(new String(data, StandardCharsets.UTF_8), DeliveryBooked.class);
            }

            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {}

            @Override
            public void close() {}
        };
    }
}
