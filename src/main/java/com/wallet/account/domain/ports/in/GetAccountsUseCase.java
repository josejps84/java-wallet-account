package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.UserType;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountDto;

import java.util.List;

public interface GetAccountsUseCase {

    List<AccountDto> getAllAccounts(int page, UserType userType, long userId);
}
