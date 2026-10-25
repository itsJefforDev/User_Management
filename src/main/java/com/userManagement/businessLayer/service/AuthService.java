package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.AuthDTOs.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}