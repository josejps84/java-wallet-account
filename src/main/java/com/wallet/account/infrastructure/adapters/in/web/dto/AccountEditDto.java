package com.wallet.account.infrastructure.adapters.in.web.dto;

import com.wallet.account.domain.model.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountEditDto(
        @NotBlank String accountNmbr,
        @Pattern(regexp = "[A-Z]{3}", message = "debe ser un código de moneda de 3 letras") String currency,
        AccountStatus status
) {
}
