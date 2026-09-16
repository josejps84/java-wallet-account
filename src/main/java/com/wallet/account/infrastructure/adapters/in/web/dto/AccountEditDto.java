package com.wallet.account.infrastructure.adapters.in.web.dto;

import com.wallet.account.domain.model.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountEditDto {
    String accountNmbr;
    String currency;
    AccountStatus status;
}