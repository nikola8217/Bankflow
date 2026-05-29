package com.bankflow.transaction.business.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferDto(
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount
) {}
