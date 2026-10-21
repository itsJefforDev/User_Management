package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.RolePermissionDTOs.*;
import com.userManagement.businessLayer.service.PermissionService;
import com.userManagement.persistenceLayer.entity.Permission;
import com.userManagement.persistenceLayer.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("El permiso ya existe: " + request.getName());
        }

        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Permission savedPermission = permissionRepository.save(permission);

        return PermissionResponse.builder()
                .id(savedPermission.getId())
                .name(savedPermission.getName())
                .description(savedPermission.getDescription())
                .build();
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}