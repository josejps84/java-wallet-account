package com.wallet.account.infrastructure.adapters.in.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import com.wallet.account.domain.model.AccountStatus;

@Builder
@Data
public class AccountDto {
   String accountNmbr;
   BigDecimal currentBalance;
   String currency;
   AccountStatus status;
   Long userId;
   Instant dateCreation;
   Instant lastUpdate;
}
