package com.mns;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

import static io.micronaut.configuration.kafka.annotation.OffsetReset.EARLIEST;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@MicronautTest
@TestInstance(PER_CLASS)
class OrderControllerTest {

    private static final Collection<Order> received = new ConcurrentLinkedDeque<>();

    @Inject
    AnalyticsListener analyticsListener;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testMessageIsPublishedToKafkaWhenBookFound() {
        String id = "1491950358";

        Optional<Order> result = retrieveGet("/orders/" + id);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().id());

        await().atMost(5, SECONDS).until(() -> !received.isEmpty());

        assertEquals(1, received.size());
        Order orderFromKafka = received.iterator().next();
        assertNotNull(orderFromKafka);
        assertEquals(id, orderFromKafka.id());
    }

    @Test
    void testMessageIsNotPublishedToKafkaWhenBookNotFound() throws Exception {
        assertThrows(HttpClientResponseException.class, () -> {
            retrieveGet("/orders/INVALID");
        });

        Thread.sleep(5_000);
        assertEquals(0, received.size());
    }

    @AfterEach
    void cleanup() {
        received.clear();
    }

    @KafkaListener(offsetReset = EARLIEST)
    static class AnalyticsListener {

        @Topic("orders")
        void updateAnalytics(Order order) {
            received.add(order);
        }
    }

    private Optional<Order> retrieveGet(String url) {
        return client.toBlocking().retrieve(HttpRequest.GET(url), Argument.of(Optional.class, Order.class));
    }
}