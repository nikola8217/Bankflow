package com.bankflow.transaction.infra.adapters;

import com.bankflow.shared.exceptions.ServiceUnavailableException;
import com.bankflow.transaction.core.exceptions.AccountNotFoundException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import com.bankflow.transaction.infra.config.AccountClientConfig;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountClientAdapterTest {

    private HttpServer server;

    private AccountClientAdapter adapterFor(HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/internal/accounts/", handler);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        String url = "http://localhost:" + server.getAddress().getPort();
        var restClient = new AccountClientConfig()
                .accountRestClient(url, Duration.ofMillis(500), Duration.ofSeconds(1));
        return new AccountClientAdapter(restClient, "test-key");
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void returnsAccountAndSendsInternalKey() throws IOException {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String[] receivedKey = new String[1];

        AccountClientAdapter adapter = adapterFor(exchange -> {
            receivedKey[0] = exchange.getRequestHeaders().getFirst("X-Internal-Api-Key");
            byte[] body = ("{\"id\":\"" + accountId + "\",\"userId\":\"" + userId
                    + "\",\"type\":\"CHECKING\",\"currency\":\"RSD\",\"status\":\"ACTIVE\"}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        AccountSnapshot account = adapter.getAccount(accountId);

        assertThat(account.userId()).isEqualTo(userId);
        assertThat(receivedKey[0]).isEqualTo("test-key");
    }

    @Test
    void notFoundBecomesAccountNotFound() throws IOException {
        AccountClientAdapter adapter = adapterFor(exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });

        assertThatThrownBy(() -> adapter.getAccount(UUID.randomUUID()))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void slowAccountServiceFailsFastWithServiceUnavailable() throws IOException {
        AccountClientAdapter adapter = adapterFor(exchange -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException ignored) {
            }
            exchange.close();
        });

        long start = System.nanoTime();

        assertThatThrownBy(() -> adapter.getAccount(UUID.randomUUID()))
                .isInstanceOf(ServiceUnavailableException.class);

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertThat(elapsed).isLessThan(Duration.ofSeconds(3));
    }
}