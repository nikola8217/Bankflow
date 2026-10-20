package com.bankflow.transaction.application.bus;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Dispatches each command to the one handler registered for its type.
 * Handlers are indexed once at startup, so two handlers for the same command fail the boot instead of a request.
 */
@Component
public class CommandBus {

    private final Map<Class<?>, CommandHandler<?, ?>> handlers;

    public CommandBus(List<CommandHandler<?, ?>> handlers) {
        this.handlers = handlers.stream().collect(Collectors.<CommandHandler<?, ?>, Class<?>, CommandHandler<?, ?>>
                toUnmodifiableMap(CommandHandler::getCommandType, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <R> R send(Command<R> command) {
        CommandHandler<Command<R>, R> handler = (CommandHandler<Command<R>, R>) handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalStateException("No handler registered for command: " + command.getClass().getSimpleName());
        }
        return handler.handle(command); // may be null for commands that return nothing (Command<Void>)
    }
}