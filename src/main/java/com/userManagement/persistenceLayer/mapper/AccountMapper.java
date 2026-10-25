package com.userManagement.persistenceLayer.mapper;

import com.userManagement.businessLayer.dto.AccountDTOs.*;
import com.userManagement.persistenceLayer.entity.Account;
import com.userManagement.persistenceLayer.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(CreateAccountRequest request, User user) {
        if (request == null) return null;
        return Account.builder()
                .name(request.getName())
                .type(request.getType())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : java.math.BigDecimal.ZERO)
                .active(true)
                .user(user)
                .build();
    }

    public AccountResponse toResponse(Account account) {
        if (account == null) return null;
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .active(account.getActive())
                .userId(account.getUser() != null ? account.getUser().getId() : null)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}