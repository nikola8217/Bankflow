package com.bankflow.transaction.application.bus;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandBusTest {

    record Ping() implements Command<Void> {}
    record Echo(String text) implements Command<String> {}

    static class PingHandler implements CommandHandler<Ping, Void> {
        int calls;
        public Void handle(Ping command) { calls++; return null; }
        public Class<Ping> getCommandType() { return Ping.class; }
    }

    static class EchoHandler implements CommandHandler<Echo, String> {
        public String handle(Echo command) { return command.text(); }
        public Class<Echo> getCommandType() { return Echo.class; }
    }

    @Test
    void commandThatReturnsNothingIsHandledOnceWithoutError() {
        PingHandler ping = new PingHandler();
        CommandBus bus = new CommandBus(List.of(ping, new EchoHandler()));

        assertThat(bus.send(new Ping())).isNull();
        assertThat(ping.calls).isEqualTo(1);
    }

    @Test
    void resultOfTheHandlerIsReturned() {
        CommandBus bus = new CommandBus(List.of(new PingHandler(), new EchoHandler()));

        assertThat(bus.send(new Echo("hi"))).isEqualTo("hi");
    }

    @Test
    void commandWithoutHandlerIsAConfigurationError() {
        CommandBus bus = new CommandBus(List.of(new EchoHandler()));

        assertThatThrownBy(() -> bus.send(new Ping()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ping");
    }

    @Test
    void twoHandlersForTheSameCommandFailAtStartup() {
        assertThatThrownBy(() -> new CommandBus(List.of(new PingHandler(), new PingHandler())))
                .isInstanceOf(IllegalStateException.class);
    }
}