package com.userManagement.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username_or_email")
    private String user; // Usuario que realizó la acción o intentó autenticarse

    @Column(nullable = false)
    private String action; // Ej: LOGIN_SUCCESS, EXPENSE_CREATED, etc.

    @Column(nullable = false)
    private String module; // Ej: AUTH, USERS, EXPENSES, ACCOUNTS, etc.

    private String resource; // Nombre de la entidad o tabla afectada

    @Column(name = "resource_id")
    private Long resourceId; // Identificador del recurso afectado

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String result; // SUCCESS, FAILED, UNAUTHORIZED

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;
}