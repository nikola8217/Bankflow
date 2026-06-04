package com.bankflow.transaction.core.commands;

public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
    Class<C> getCommandType();
}