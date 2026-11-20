package com.wallet.account.infrastructure.adapters.in.web.dto;

import java.util.List;

public record AccountPageDto(List<AccountDto> accounts, long totalItems) {
}
