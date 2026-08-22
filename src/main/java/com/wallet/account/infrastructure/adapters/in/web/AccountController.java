package com.wallet.account.infrastructure.adapters.in.web;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.AccountRequest;
import com.wallet.account.domain.ports.in.GetBalanceUseCase;
import com.wallet.account.domain.ports.in.WithdrawUseCase;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountDto;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountMapper;
import com.wallet.account.domain.ports.in.CreateAccountUseCase;
import com.wallet.account.domain.ports.in.DepositUseCase;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController{

    private final CreateAccountUseCase createAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final GetBalanceUseCase getBalanceUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase, DepositUseCase depositUseCase, WithdrawUseCase withdrawUseCase, GetBalanceUseCase getBalanceUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
    }

    @PostMapping("/new")
    public ResponseEntity<String> newAccount(@RequestBody AccountDto request) {
        createAccountUseCase.createAccount(AccountMapper.toDomain(request));
        return ResponseEntity.ok("Cuenta creada exitosamente");
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> deposit(@RequestBody AccountRequest request) {
        depositUseCase.deposit(request.email(), request.amount());
        return ResponseEntity.ok(Map.of("status", "Depósito realizado exitosamente"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, String>> withdraw(@RequestBody AccountRequest request) {
        withdrawUseCase.withdraw(request.email(), request.amount());
        return ResponseEntity.ok(Map.of("status", "Retiro realizado exitosamente"));
    }

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<Map<String, Double>> getBalance(@PathVariable String accountId) {
        double balance = getBalanceUseCase.getBalance(accountId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }
}
