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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
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
class OrderController1Test {

    private static final Collection<Order> received = new ConcurrentLinkedDeque<>();

    @Inject
    AnalyticsListener analyticsListener;

    @Inject
    @Client("/")
    HttpClient client;
    @BeforeAll
    void startKafka(){
        try {
// Build the command to start the Kafka container
            String[] command = {"podman", "run", "-d", "--name", "kafka", "-p", "9092:9092", "confluentinc/cp-kafka"};

// Create a process builder with the command
            ProcessBuilder processBuilder = new ProcessBuilder(command);

// Start the process
            Process process = processBuilder.start();

// Wait for the process to complete
            int exitCode = process.waitFor();

// Check the exit code
            if (exitCode == 0) {
                System.out.println("Kafka started successfully.");
            } else {
                System.err.println("Failed to start Kafka.");
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    void stopKafka() {
        try {
            // Build the command to stop the Kafka container
            String[] command = {"podman", "stop", "kafka"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Kafka stopped successfully.");
            } else {
                System.err.println("Failed to stop Kafka.");
            }

        }catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
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