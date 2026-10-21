package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.RolePermissionDTOs.*;
import com.userManagement.businessLayer.service.RoleService;
import com.userManagement.persistenceLayer.entity.Permission;
import com.userManagement.persistenceLayer.entity.Role;
import com.userManagement.persistenceLayer.repository.PermissionRepository;
import com.userManagement.persistenceLayer.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("El rol ya existe: " + request.getName());
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissions() != null) {
            for (String permName : request.getPermissions()) {
                Permission perm = permissionRepository.findByName(permName)
                        .orElseThrow(() -> new RuntimeException("Permiso no encontrado: " + permName));
                permissions.add(perm);
            }
        }

        Role role = Role.builder()
                .name(request.getName())
                .permissions(permissions)
                .build();

        Role savedRole = roleRepository.save(role);
        return mapToResponse(savedRole);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + name));
        return mapToResponse(role);
    }

    private RoleResponse mapToResponse(Role role) {
        Set<String> permNames = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(permNames)
                .build();
    }
}