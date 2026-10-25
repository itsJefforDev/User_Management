package com.userManagement.userManagement.businessLayer.impl;

import com.userManagement.businessLayer.dto.AuditDTOs.*;
import com.userManagement.businessLayer.service.impl.AuditServiceImpl;
import com.userManagement.persistenceLayer.entity.AuditLog;
import com.userManagement.persistenceLayer.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para AuditServiceImpl
 *
 * OBJETIVO: Probar la lógica de negocio del servicio de auditoría de forma aislada
 * - Cobertura del 100% de los métodos (registro de eventos, consulta general y filtros avanzados)
 * - Sin base de datos ni Spring Context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService - Unit Tests")
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLog validAuditLog;
    private CreateAuditLogRequest fullRequest;
    private CreateAuditLogRequest minimalRequest;

    @BeforeEach
    void setUp() {
        validAuditLog = AuditLog.builder()
                .id(1L)
                .user("john.doe@email.com")
                .action("LOGIN")
                .module("AUTH")
                .resource("User")
                .resourceId(10L)
                .timestamp(LocalDateTime.now())
                .result("SUCCESS")
                .ipAddress("127.0.0.1")
                .additionalInfo("Inicio de sesión exitoso")
                .build();

        fullRequest = CreateAuditLogRequest.builder()
                .user("john.doe@email.com")
                .action("LOGIN")
                .module("AUTH")
                .resource("User")
                .resourceId(10L)
                .result("SUCCESS")
                .ipAddress("127.0.0.1")
                .additionalInfo("Inicio de sesión exitoso")
                .build();

        minimalRequest = CreateAuditLogRequest.builder()
                .user(null)
                .action("DELETE")
                .module("USERS")
                .resource("User")
                .resourceId(5L)
                .result(null)
                .ipAddress("192.168.1.50")
                .additionalInfo("Eliminación de usuario")
                .build();
    }

    // ==================== LOG EVENT TESTS ====================

    @Test
    @DisplayName("LOG EVENT - Con usuario y resultado explícitos debe guardar correctamente")
    void logEvent_FullRequest_ShouldSaveAuditLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(validAuditLog);

        auditService.logEvent(fullRequest);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("LOG EVENT - Con usuario nulo y resultado nulo debe asignar valores por defecto SYSTEM y SUCCESS")
    void logEvent_NullUserAndResult_ShouldAssignDefaults() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(validAuditLog);

        auditService.logEvent(minimalRequest);

        verify(auditLogRepository, times(1)).save(argThat(log ->
                log.getUser().equals("SYSTEM") && log.getResult().equals("SUCCESS")
        ));
    }

    // ==================== GET ALL LOGS TESTS ====================

    @Test
    @DisplayName("GET ALL LOGS - Debe retornar lista mapeada de AuditLogResponse")
    void getAllLogs_ShouldReturnAuditLogResponseList() {
        when(auditLogRepository.findAll()).thenReturn(Collections.singletonList(validAuditLog));

        List<AuditLogResponse> responses = auditService.getAllLogs();

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getUser()).isEqualTo("john.doe@email.com");
        assertThat(responses.get(0).getAction()).isEqualTo("LOGIN");
        assertThat(responses.get(0).getModule()).isEqualTo("AUTH");

        verify(auditLogRepository, times(1)).findAll();
    }

    // ==================== GET LOGS WITH FILTERS TESTS ====================

    @Test
    @DisplayName("GET LOGS WITH FILTERS - Debe retornar lista filtrada de AuditLogResponse")
    void getLogsWithFilters_ShouldReturnFilteredAuditLogResponseList() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        when(auditLogRepository.findWithFilters("john.doe@email.com", "AUTH", "LOGIN", startDate, endDate))
                .thenReturn(Collections.singletonList(validAuditLog));

        List<AuditLogResponse> responses = auditService.getLogsWithFilters(
                "john.doe@email.com", "AUTH", "LOGIN", startDate, endDate
        );

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUser()).isEqualTo("john.doe@email.com");

        verify(auditLogRepository, times(1))
                .findWithFilters("john.doe@email.com", "AUTH", "LOGIN", startDate, endDate);
    }
}