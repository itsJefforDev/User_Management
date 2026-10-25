package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.AccountDTOs.*;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request);
    AccountResponse getAccountById(Long id);
    List<AccountResponse> getAccountsByUser(Long userId);
    AccountResponse updateAccount(Long id, UpdateAccountRequest request);
    void deactivateAccount(Long id);
    BigDecimal getAccountBalance(Long id);
}