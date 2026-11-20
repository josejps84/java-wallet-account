package com.wallet.account.infrastructure.adapters.in.web.dto;

import java.math.BigDecimal;

public record BalanceDto(String accountNmbr, BigDecimal balance, String currency) {
}
