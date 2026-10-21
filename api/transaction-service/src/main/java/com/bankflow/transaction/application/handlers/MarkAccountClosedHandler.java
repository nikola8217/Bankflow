package com.bankflow.transaction.application.handlers;

import com.bankflow.transaction.application.bus.CommandHandler;
import com.bankflow.transaction.application.commands.MarkAccountClosedCommand;
import com.bankflow.transaction.application.ports.AccountDirectoryWriter;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MarkAccountClosedHandler implements CommandHandler<MarkAccountClosedCommand, Void> {

    private final AccountDirectoryWriter accounts;

    @Override
    public Class<MarkAccountClosedCommand> getCommandType() {
        return MarkAccountClosedCommand.class;
    }

    @Override
    @Transactional
    public Void handle(MarkAccountClosedCommand command) {
        accounts.markClosed(new AccountSnapshot(
                command.accountId(), command.userId(), command.currency(), "CLOSED"));

        return null;
    }
}