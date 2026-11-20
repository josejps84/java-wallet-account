package com.wallet.account.infrastructure.adapters.in.web;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.Caller;
import com.wallet.account.domain.model.PagedResult;
import com.wallet.account.domain.ports.in.CreateAccountUseCase;
import com.wallet.account.domain.ports.in.DepositUseCase;
import com.wallet.account.domain.ports.in.EditAccountUseCase;
import com.wallet.account.domain.ports.in.GetAccountsUseCase;
import com.wallet.account.domain.ports.in.GetBalanceUseCase;
import com.wallet.account.domain.ports.in.WithdrawUseCase;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountCreationDto;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountDto;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountEditDto;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountPageDto;
import com.wallet.account.infrastructure.adapters.in.web.dto.AmountRequestDto;
import com.wallet.account.infrastructure.adapters.in.web.dto.BalanceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final GetAccountsUseCase getAccountsUseCase;
    private final EditAccountUseCase editAccountUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase, DepositUseCase depositUseCase,
                             WithdrawUseCase withdrawUseCase, GetBalanceUseCase getBalanceUseCase,
                             GetAccountsUseCase getAccountsUseCase, EditAccountUseCase editAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
        this.getAccountsUseCase = getAccountsUseCase;
        this.editAccountUseCase = editAccountUseCase;
    }

    @GetMapping
    public AccountPageDto getAllAccounts(@AuthenticationPrincipal Caller caller,
                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                         @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        PagedResult<Account> result = getAccountsUseCase.getAccounts(caller, page, pageSize);
        return new AccountPageDto(result.content().stream().map(AccountDto::from).toList(), result.totalElements());
    }

    @PostMapping("/new")
    public ResponseEntity<AccountDto> newAccount(@AuthenticationPrincipal Caller caller,
                                                 @Valid @RequestBody(required = false) AccountCreationDto request) {
        Long userId = request != null ? request.userId() : null;
        String currency = request != null ? request.currency() : null;
        Account account = createAccountUseCase.createAccount(caller, userId, currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountDto.from(account));
    }

    @PostMapping("/deposit")
    public AccountDto deposit(@AuthenticationPrincipal Caller caller, @Valid @RequestBody AmountRequestDto request) {
        return AccountDto.from(depositUseCase.deposit(caller, request.accountId(), request.amount()));
    }

    @PostMapping("/withdraw")
    public AccountDto withdraw(@AuthenticationPrincipal Caller caller, @Valid @RequestBody AmountRequestDto request) {
        return AccountDto.from(withdrawUseCase.withdraw(caller, request.accountId(), request.amount()));
    }

    @GetMapping("/balance/{accountId}")
    public BalanceDto getBalance(@AuthenticationPrincipal Caller caller, @PathVariable String accountId) {
        Account account = getBalanceUseCase.getAccount(caller, accountId);
        return new BalanceDto(account.accountNmbr(), account.currentBalance(), account.currency());
    }

    @PutMapping("/edit")
    public AccountDto editAccount(@AuthenticationPrincipal Caller caller, @Valid @RequestBody AccountEditDto request) {
        return AccountDto.from(editAccountUseCase.editAccount(caller, request.accountNmbr(), request.currency(),
                request.status()));
    }
}
