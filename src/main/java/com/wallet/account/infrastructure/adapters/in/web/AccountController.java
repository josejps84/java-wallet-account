package com.wallet.account.infrastructure.adapters.in.web;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.PagedResult;
import com.wallet.account.domain.model.UserType;
import com.wallet.account.domain.ports.in.*;
import com.wallet.account.infrastructure.adapters.in.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@Slf4j
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final GetAccountsUseCase getAccountsUseCase;
    private final EditAccountUseCase editAccountUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase, DepositUseCase depositUseCase,
                            WithdrawUseCase withdrawUseCase, GetBalanceUseCase getBalanceUseCase,
                            GetAccountsUseCase getAccountsUseCase,
                            EditAccountUseCase editAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
        this.getAccountsUseCase = getAccountsUseCase;
        this.editAccountUseCase = editAccountUseCase;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAccounts(@RequestParam(name="page", required = false) Integer page,
                                                              @RequestParam(name="pageSize", required = false) Integer pageSize,
                                                              HttpServletRequest httpRequest) {
        UserType userType = HttpUtils.getAttributeFromRequest(httpRequest, "userType", UserType.CUSTOMER);
        long userId       = HttpUtils.getAttributeFromRequest(httpRequest, "userId", 0L);

        PagedResult<AccountDto> accountsPage = getAccountsUseCase.getAllAccounts(page == null ? 0 : page,
                pageSize == null ? 20 : pageSize, userType, userId);

        return ResponseEntity.ok(Map.of(
                "accounts", accountsPage.content(),
                "totalItems", accountsPage.totalElements()));
    }

    @PostMapping("/new")
    public ResponseEntity<String> newAccount(@RequestBody AccountCreationDto request) {
        Account account = AccountMapper.toDomain(request);
        createAccountUseCase.createAccount(account);
        return ResponseEntity.ok("Cuenta creada exitosamente");
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> deposit(@RequestBody AccountRequest request) {
        depositUseCase.deposit(request.getAccountId(), request.getAmount());
        return ResponseEntity.ok(Map.of("status", "Depósito realizado exitosamente"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, String>> withdraw(@RequestBody AccountRequest request) {
        withdrawUseCase.withdraw(request.getAccountId(), request.getAmount());
        return ResponseEntity.ok(Map.of("status", "Retiro realizado exitosamente"));
    }

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<Map<String, Double>> getBalance(@PathVariable String accountId) {
        double balance = getBalanceUseCase.getBalance(accountId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @PutMapping("/edit")
    public ResponseEntity<String> editAccount(@RequestBody AccountEditDto request) {
        Account account = AccountMapper.toDomain(request);
        editAccountUseCase.editAccount(account);
        return ResponseEntity.ok("Cuenta editada exitosamente");
    }
}
