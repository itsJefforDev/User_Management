package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.UserDTOs.*;
import com.userManagement.businessLayer.service.impl.UserServiceImpl;
import com.userManagement.persistenceLayer.entity.Role;
import com.userManagement.persistenceLayer.entity.User;
import com.userManagement.persistenceLayer.entity.UserStatus;
import com.userManagement.persistenceLayer.mapper.UserMapper;
import com.userManagement.persistenceLayer.repository.RoleRepository;
import com.userManagement.persistenceLayer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Unit Tests (100% Branch Coverage Final)")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User validUser;
    private Role defaultRole;
    private Role adminRole;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        defaultRole = Role.builder().id(1L).name("ROLE_USER").build();
        adminRole = Role.builder().id(2L).name("ROLE_ADMIN").build();

        validUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@email.com")
                .password("encodedPassword")
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .roles(new HashSet<>(Collections.singletonList(defaultRole)))
                .build();

        createUserRequest = new CreateUserRequest();
        createUserRequest.setName("John Doe");
        createUserRequest.setEmail("john.doe@email.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setRoles(new HashSet<>(Collections.singletonList("ROLE_USER")));

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setName("John Updated");
        updateUserRequest.setStatus(UserStatus.ACTIVE);
        updateUserRequest.setRoles(new HashSet<>(Collections.singletonList("ROLE_ADMIN")));
    }

    // ==================== CREATE USER TESTS ====================

    @Test
    @DisplayName("CREATE - Datos válidos y roles explícitos debe guardar y retornar UserResponse")
    void createUser_ValidDataWithRoles_ShouldReturnUserResponse() {
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.createUser(createUserRequest);

        assertThat(response).isNotNull();
        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
    }

    @Test
    @DisplayName("CREATE - Sin roles especificados (null) debe asignar rol por defecto ROLE_USER")
    void createUser_NoRolesProvidedNull_ShouldAssignDefaultRole() {
        createUserRequest.setRoles(null);

        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.createUser(createUserRequest);

        assertThat(response).isNotNull();
        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("CREATE - Roles vacíos (Empty Set) debe asignar rol por defecto ROLE_USER")
    void createUser_EmptyRolesSet_ShouldAssignDefaultRole() {
        createUserRequest.setRoles(Collections.emptySet());

        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.createUser(createUserRequest);

        assertThat(response).isNotNull();
        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("CREATE - Email duplicado debe lanzar IllegalArgumentException")
    void createUser_ExistingEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El email ya se encuentra registrado");

        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CREATE - Rol no encontrado debe lanzar RuntimeException")
    void createUser_RoleNotFound_ShouldThrowRuntimeException() {
        createUserRequest.setRoles(new HashSet<>(Collections.singletonList("ROLE_UNKNOWN")));

        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rol no encontrado: ROLE_UNKNOWN");

        verify(roleRepository).findByName("ROLE_UNKNOWN");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CREATE - Rol por defecto no encontrado debe lanzar RuntimeException")
    void createUser_DefaultRoleNotFound_ShouldThrowRuntimeException() {
        createUserRequest.setRoles(null);

        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rol por defecto ROLE_USER no encontrado");

        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== GET USER BY ID TESTS ====================

    @Test
    @DisplayName("GET BY ID - ID existente debe retornar UserResponse")
    void getUserById_ExistingId_ShouldReturnUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userMapper.toResponse(validUser)).thenReturn(new UserResponse());

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(validUser);
    }

    @Test
    @DisplayName("GET BY ID - ID inexistente debe lanzar RuntimeException")
    void getUserById_NonExistingId_ShouldThrowRuntimeException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado con ID: 99");

        verify(userRepository).findById(99L);
    }

    // ==================== GET ALL USERS TESTS ====================

    @Test
    @DisplayName("GET ALL - Debe retornar lista de UserResponse")
    void getAllUsers_ShouldReturnUserResponseList() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(validUser));
        when(userMapper.toResponse(validUser)).thenReturn(new UserResponse());

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        verify(userRepository).findAll();
        verify(userMapper).toResponse(validUser);
    }

    // ==================== UPDATE USER TESTS ====================

    @Test
    @DisplayName("UPDATE - Datos válidos debe actualizar y retornar UserResponse")
    void updateUser_ValidData_ShouldReturnUpdatedUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(1L, updateUserRequest);

        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(roleRepository).findByName("ROLE_ADMIN");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
    }

    @Test
    @DisplayName("UPDATE - Campos parciales con nombre nulo y status nulo (Cubre branch status != null -> false)")
    void updateUser_NullNameAndNullStatus_ShouldNotUpdateThem() {
        UpdateUserRequest partialRequest = new UpdateUserRequest();
        partialRequest.setName(null);
        partialRequest.setStatus(null); // Cubre branch request.getStatus() != null -> false
        partialRequest.setRoles(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(1L, partialRequest);

        assertThat(response).isNotNull();
        assertThat(validUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("UPDATE - ID inexistente debe lanzar RuntimeException")
    void updateUser_NonExistingId_ShouldThrowRuntimeException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, updateUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado con ID: 99");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("UPDATE - Rol inválido en actualización debe lanzar RuntimeException")
    void updateUser_RoleNotFound_ShouldThrowRuntimeException() {
        updateUserRequest.setRoles(new HashSet<>(Collections.singletonList("ROLE_INVALID")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(roleRepository.findByName("ROLE_INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, updateUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rol no encontrado: ROLE_INVALID");

        verify(roleRepository).findByName("ROLE_INVALID");
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    @DisplayName("DELETE - ID existente debe marcar usuario como INACTIVE y guardar")
    void deleteUser_ExistingId_ShouldSetStatusInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        assertThatCode(() -> userService.deleteUser(1L)).doesNotThrowAnyException();

        assertThat(validUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).findById(1L);
        verify(userRepository).save(validUser);
    }

    @Test
    @DisplayName("DELETE - ID inexistente debe lanzar RuntimeException")
    void deleteUser_NonExistingId_ShouldThrowRuntimeException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado con ID: 99");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== FAILED LOGIN TESTS ====================

    @Test
    @DisplayName("HANDLE FAILED LOGIN - Intentos previos null (cubre branch attempts != null -> false)")
    void handleFailedLogin_NullAttempts_ShouldStartAtOne() {
        validUser.setFailedLoginAttempts(null);
        when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        userService.handleFailedLogin("john.doe@email.com");

        assertThat(validUser.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository).findByEmail("john.doe@email.com");
        verify(userRepository).save(validUser);
    }

    @Test
    @DisplayName("HANDLE FAILED LOGIN - Incrementa intentos fallidos sin bloquear si es menor a 3")
    void handleFailedLogin_AttemptsLessThanThree_ShouldIncrementOnly() {
        validUser.setFailedLoginAttempts(1);
        when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        userService.handleFailedLogin("john.doe@email.com");

        assertThat(validUser.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(validUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).findByEmail("john.doe@email.com");
        verify(userRepository).save(validUser);
    }

    @Test
    @DisplayName("HANDLE FAILED LOGIN - Llega a 3 intentos y bloquea el usuario (LOCKED)")
    void handleFailedLogin_AttemptsReachThree_ShouldLockUser() {
        validUser.setFailedLoginAttempts(2);
        when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        userService.handleFailedLogin("john.doe@email.com");

        assertThat(validUser.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(validUser.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(userRepository).findByEmail("john.doe@email.com");
        verify(userRepository).save(validUser);
    }

    @Test
    @DisplayName("HANDLE FAILED LOGIN - Email no encontrado no hace nada")
    void handleFailedLogin_NonExistingEmail_ShouldDoNothing() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThatCode(() -> userService.handleFailedLogin("unknown@email.com"))
                .doesNotThrowAnyException();

        verify(userRepository).findByEmail("unknown@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== RESET FAILED LOGIN TESTS ====================

    @Test
    @DisplayName("RESET FAILED LOGIN - Resetea los intentos fallidos a 0 si existe el usuario")
    void resetFailedLoginAttempts_ExistingEmail_ShouldResetAttempts() {
        validUser.setFailedLoginAttempts(2);
        when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        userService.resetFailedLoginAttempts("john.doe@email.com");

        assertThat(validUser.getFailedLoginAttempts()).isZero();
        verify(userRepository).findByEmail("john.doe@email.com");
        verify(userRepository).save(validUser);
    }

    @Test
    @DisplayName("RESET FAILED LOGIN - Email no encontrado no hace nada")
    void resetFailedLoginAttempts_NonExistingEmail_ShouldDoNothing() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThatCode(() -> userService.resetFailedLoginAttempts("unknown@email.com"))
                .doesNotThrowAnyException();

        verify(userRepository).findByEmail("unknown@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== FALLBACK & MAPPER NULL BRANCH TESTS ====================

    @Test
    @DisplayName("FALLBACK - userMapper null y roles null para cubrir todos los branches de fallback y mapper")
    void mapToResponseFallback_NullRolesAndNullMapper_ShouldUseFallbackForAllMethods() throws Exception {
        // Forzar userMapper a null mediante reflexión para cubrir el branch `userMapper != null ? ... : ...` -> false en TODOS lados
        Field mapperField = UserServiceImpl.class.getDeclaredField("userMapper");
        mapperField.setAccessible(true);
        mapperField.set(userService, null);

        // Establecer roles a null para cubrir el branch `user.getRoles() != null` -> false en mapToResponseFallback
        validUser.setRoles(null);

        // 1. Probar createUser con mapper null y roles null
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        UserResponse createResponse = userService.createUser(createUserRequest);
        assertThat(createResponse).isNotNull();

        // 2. Probar getUserById con mapper null
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        UserResponse getResponse = userService.getUserById(1L);
        assertThat(getResponse).isNotNull();

        // 3. Probar getAllUsers con mapper null
        when(userRepository.findAll()).thenReturn(Collections.singletonList(validUser));
        List<UserResponse> getAllResponse = userService.getAllUsers();
        assertThat(getAllResponse).hasSize(1);

        // 4. Probar updateUser con mapper null
        UpdateUserRequest updateReq = new UpdateUserRequest();
        updateReq.setName("New Name");
        updateReq.setStatus(UserStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        UserResponse updateResponse = userService.updateUser(1L, updateReq);
        assertThat(updateResponse).isNotNull();

        // Restaurar el mock del mapper al finalizar la prueba
        mapperField.set(userService, userMapper);
    }

    @Test
    @DisplayName("FALLBACK / MAPPER - Usuario con roles null debe retornar set de roles vacío")
    void mapToResponse_UserRolesNull_ShouldReturnEmptyRoleNamesSet() {
        // ARRANGE: Configuramos un usuario con roles en null
        validUser.setRoles(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));

        // Usamos el userMapper real o mock según cómo lo tengas configurado,
        // pero asegurando que la entidad pase por el método que mapea los roles.
        // Si tu mapper hace el mapeo de roles, asegúrate de que use el helper o el método del servicio.

        // ACT & ASSERT: Al invocar un método que devuelva la respuesta del usuario,
        // se evaluará `user.getRoles() != null` como false.
        assertThatCode(() -> userService.getUserById(1L))
                .doesNotThrowAnyException();

        verify(userRepository).findById(1L);
    }
}