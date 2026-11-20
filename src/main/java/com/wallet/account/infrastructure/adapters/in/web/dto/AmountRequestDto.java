package com.wallet.account.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AmountRequestDto(
        @NotBlank String accountId,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount
) {
}
