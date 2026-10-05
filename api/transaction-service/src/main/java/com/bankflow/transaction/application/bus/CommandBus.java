package com.bankflow.transaction.application.bus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class CommandBus {

    private final List<CommandHandler<?, ?>> handlers;

    @SuppressWarnings("unchecked")
    public <R> R send(Command<R> command) {
        return handlers.stream()
                .filter(h -> h.getCommandType().equals(command.getClass()))
                .findFirst()
                .map(h -> ((CommandHandler<Command<R>, R>) h).handle(command))
                .orElseThrow(() -> new RuntimeException(
                        "No handler found for command: " + command.getClass().getSimpleName()
                ));
    }
}