package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.RolePermissionDTOs.*;
import com.userManagement.businessLayer.service.impl.PermissionServiceImpl;
import com.userManagement.persistenceLayer.entity.Permission;
import com.userManagement.persistenceLayer.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para PermissionServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de permisos de forma aislada
 * - Cobertura del 100% de los métodos y caminos alternativos (creación de permisos, validación de duplicados y consultas)
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService - Unit Tests")
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission validPermission;
    private CreatePermissionRequest createPermissionRequest;

    @BeforeEach
    void setUp() {
        validPermission = Permission.builder()
                .id(1L)
                .name("READ_PRIVILEGE")
                .description("Read privilege description")
                .build();

        createPermissionRequest = new CreatePermissionRequest();
        createPermissionRequest.setName("READ_PRIVILEGE");
        createPermissionRequest.setDescription("Read privilege description");
    }

    // ==================== CREATE PERMISSION TESTS ====================

    @Test
    @DisplayName("CREATE - Datos válidos debe guardar y retornar PermissionResponse")
    void createPermission_ValidData_ShouldReturnPermissionResponse() {
        when(permissionRepository.findByName(createPermissionRequest.getName())).thenReturn(Optional.empty());
        when(permissionRepository.save(any(Permission.class))).thenReturn(validPermission);

        PermissionResponse response = permissionService.createPermission(createPermissionRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("READ_PRIVILEGE");
        assertThat(response.getDescription()).isEqualTo("Read privilege description");

        verify(permissionRepository).findByName(createPermissionRequest.getName());
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("CREATE - Permiso ya existente debe lanzar RuntimeException")
    void createPermission_ExistingName_ShouldThrowRuntimeException() {
        when(permissionRepository.findByName(createPermissionRequest.getName())).thenReturn(Optional.of(validPermission));

        assertThatThrownBy(() -> permissionService.createPermission(createPermissionRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El permiso ya existe: READ_PRIVILEGE");

        verify(permissionRepository).findByName(createPermissionRequest.getName());
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    // ==================== GET ALL PERMISSIONS TESTS ====================

    @Test
    @DisplayName("GET ALL - Debe retornar lista de PermissionResponse")
    void getAllPermissions_ShouldReturnPermissionResponseList() {
        when(permissionRepository.findAll()).thenReturn(Collections.singletonList(validPermission));

        List<PermissionResponse> responses = permissionService.getAllPermissions();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getName()).isEqualTo("READ_PRIVILEGE");
        assertThat(responses.get(0).getDescription()).isEqualTo("Read privilege description");

        verify(permissionRepository).findAll();
    }
}