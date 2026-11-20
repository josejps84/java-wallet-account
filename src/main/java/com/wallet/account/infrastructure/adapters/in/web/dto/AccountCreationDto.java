package com.wallet.account.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Both fields are optional: by default the account is opened for the caller in USD.
 * {@code userId} is only honoured for admins.
 */
public record AccountCreationDto(
        @Positive Long userId,
        @Pattern(regexp = "[A-Z]{3}", message = "debe ser un código de moneda de 3 letras") String currency
) {
}
