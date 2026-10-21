package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.RolePermissionDTOs.*;
import java.util.List;

public interface RoleService {
    RoleResponse createRole(CreateRoleRequest request);
    List<RoleResponse> getAllRoles();
    RoleResponse getRoleByName(String name);
}