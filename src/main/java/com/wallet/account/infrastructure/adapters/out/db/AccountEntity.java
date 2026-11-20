package com.wallet.account.infrastructure.adapters.out.db;

import com.wallet.account.domain.model.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class AccountEntity {

    @Id
    @Column(name = "account_nmbr", nullable = false, length = 50)
    private String accountNmbr;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Convert(converter = AccountStatusConverter.class)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @Column(name = "last_update", nullable = false)
    private Instant lastUpdate;
}
