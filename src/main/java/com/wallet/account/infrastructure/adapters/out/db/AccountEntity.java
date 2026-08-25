package com.wallet.account.infrastructure.adapters.out.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class AccountEntity {

    @Id
    @Column(name = "account_nmbr", nullable = false, unique = true, length = 50)
    private String accountNmbr;

    @Column(name = "current_balance", precision = 19, scale = 4)
    private BigDecimal currentBalance;

    @Column(name = "currency")
    private String currency;

    @Column(name = "status")
    private Integer status;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "date_creation")
    private Instant dateCreation;

    @Column(name = "last_update")
    private Instant lastUpdate;
}