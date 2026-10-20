package com.bankflow.transaction.application.handlers;

import com.bankflow.transaction.application.bus.CommandHandler;
import com.bankflow.transaction.application.commands.RecordAccountCommand;
import com.bankflow.transaction.application.ports.AccountDirectoryWriter;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RecordAccountHandler implements CommandHandler<RecordAccountCommand, Void> {

    private final AccountDirectoryWriter accounts;

    @Override
    public Class<RecordAccountCommand> getCommandType() {
        return RecordAccountCommand.class;
    }

    @Override
    @Transactional
    public Void handle(RecordAccountCommand command) {
        accounts.addIfAbsent(new AccountSnapshot(
                command.accountId(), command.userId(), command.currency(), "ACTIVE"));
        return null;
    }
}