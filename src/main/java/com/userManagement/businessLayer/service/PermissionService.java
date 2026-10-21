package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.RolePermissionDTOs.*;
import java.util.List;

public interface PermissionService {
    PermissionResponse createPermission(CreatePermissionRequest request);
    List<PermissionResponse> getAllPermissions();

}