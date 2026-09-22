package com.wallet.account.domain.ports.in;

import com.wallet.account.domain.model.PagedResult;
import com.wallet.account.domain.model.UserType;
import com.wallet.account.infrastructure.adapters.in.web.dto.AccountDto;

public interface GetAccountsUseCase {

    PagedResult<AccountDto> getAllAccounts(int page, int pageSize, UserType userType, long userId);
}
