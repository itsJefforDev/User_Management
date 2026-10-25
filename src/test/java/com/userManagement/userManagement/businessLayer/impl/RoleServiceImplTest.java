package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.RolePermissionDTOs.*;
import com.userManagement.businessLayer.service.impl.RoleServiceImpl;
import com.userManagement.persistenceLayer.entity.Permission;
import com.userManagement.persistenceLayer.entity.Role;
import com.userManagement.persistenceLayer.repository.PermissionRepository;
import com.userManagement.persistenceLayer.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para RoleServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de roles de forma aislada
 * - Cobertura del 100% de los métodos y caminos alternativos (creación de roles, consultas por ID/nombre, mapeo de permisos)
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService - Unit Tests")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role validRole;
    private Permission validPermission;
    private CreateRoleRequest createRoleRequest;

    @BeforeEach
    void setUp() {
        validPermission = Permission.builder()
                .id(1L)
                .name("READ_PRIVILEGE")
                .description("Read privilege")
                .build();

        validRole = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .permissions(new HashSet<>(Collections.singletonList(validPermission)))
                .build();

        createRoleRequest = new CreateRoleRequest();
        createRoleRequest.setName("ROLE_ADMIN");
        createRoleRequest.setPermissions(new HashSet<>(Collections.singletonList("READ_PRIVILEGE")));
    }

    // ==================== CREATE ROLE TESTS ====================

    @Test
    @DisplayName("CREATE - Datos válidos y permisos explícitos debe guardar y retornar RoleResponse")
    void createRole_ValidDataWithPermissions_ShouldReturnRoleResponse() {
        when(roleRepository.findByName(createRoleRequest.getName())).thenReturn(Optional.empty());
        when(permissionRepository.findByName("READ_PRIVILEGE")).thenReturn(Optional.of(validPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(validRole);

        RoleResponse response = roleService.createRole(createRoleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getPermissions()).contains("READ_PRIVILEGE");

        verify(roleRepository).findByName(createRoleRequest.getName());
        verify(permissionRepository).findByName("READ_PRIVILEGE");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("CREATE - Sin permisos especificados debe crear el rol sin permisos adicionales")
    void createRole_NoPermissionsProvided_ShouldReturnRoleResponse() {
        createRoleRequest.setPermissions(null);
        Role roleWithoutPermissions = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .permissions(new HashSet<>())
                .build();

        when(roleRepository.findByName(createRoleRequest.getName())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(roleWithoutPermissions);

        RoleResponse response = roleService.createRole(createRoleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getPermissions()).isEmpty();

        verify(roleRepository).findByName(createRoleRequest.getName());
        verify(permissionRepository, never()).findByName(any());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("CREATE - Rol ya existente debe lanzar RuntimeException")
    void createRole_ExistingRole_ShouldThrowRuntimeException() {
        when(roleRepository.findByName(createRoleRequest.getName())).thenReturn(Optional.of(validRole));

        assertThatThrownBy(() -> roleService.createRole(createRoleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El rol ya existe: ROLE_ADMIN");

        verify(roleRepository).findByName(createRoleRequest.getName());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("CREATE - Permiso no encontrado debe lanzar RuntimeException")
    void createRole_PermissionNotFound_ShouldThrowRuntimeException() {
        createRoleRequest.setPermissions(new HashSet<>(Collections.singletonList("UNKNOWN_PERMISSION")));

        when(roleRepository.findByName(createRoleRequest.getName())).thenReturn(Optional.empty());
        when(permissionRepository.findByName("UNKNOWN_PERMISSION")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.createRole(createRoleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Permiso no encontrado: UNKNOWN_PERMISSION");

        verify(roleRepository).findByName(createRoleRequest.getName());
        verify(permissionRepository).findByName("UNKNOWN_PERMISSION");
        verify(roleRepository, never()).save(any(Role.class));
    }

    // ==================== GET ALL ROLES TESTS ====================

    @Test
    @DisplayName("GET ALL - Debe retornar lista de RoleResponse")
    void getAllRoles_ShouldReturnRoleResponseList() {
        when(roleRepository.findAll()).thenReturn(Collections.singletonList(validRole));

        List<RoleResponse> responses = roleService.getAllRoles();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("ROLE_ADMIN");
        assertThat(responses.get(0).getPermissions()).contains("READ_PRIVILEGE");

        verify(roleRepository).findAll();
    }

    // ==================== GET ROLE BY NAME TESTS ====================

    @Test
    @DisplayName("GET BY NAME - Nombre existente debe retornar RoleResponse")
    void getRoleByName_ExistingName_ShouldReturnRoleResponse() {
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(validRole));

        RoleResponse response = roleService.getRoleByName("ROLE_ADMIN");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("ROLE_ADMIN");

        verify(roleRepository).findByName("ROLE_ADMIN");
    }

    @Test
    @DisplayName("GET BY NAME - Nombre inexistente debe lanzar RuntimeException")
    void getRoleByName_NonExistingName_ShouldThrowRuntimeException() {
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleByName("ROLE_UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rol no encontrado: ROLE_UNKNOWN");

        verify(roleRepository).findByName("ROLE_UNKNOWN");
    }
}