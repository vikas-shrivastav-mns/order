package com.mns;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.MessageHeader;
import reactor.core.publisher.Mono;

@KafkaClient
public interface AnalyticsClient {

    @Topic("orders")
    Mono<Order> updateAnalytics(@KafkaKey String orderId, Order order, @MessageHeader("X-Custom-Header") String header);
}