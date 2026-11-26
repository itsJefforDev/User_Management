package com.userManagement.persistenceLayer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    /** Token de activación por enlace (cuentas creadas por el administrador). Nullable. */
    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    /** Código de 4 dígitos para activar cuentas auto-registradas. */
    @Column(name = "verification_code", length = 4)
    private String verificationCode;

    @Column(name = "verification_code_expires_at")
    private LocalDateTime verificationCodeExpiresAt;

    /** Intentos fallidos al digitar el código (protege la fuerza bruta de 4 dígitos). */
    @Column(name = "verification_code_attempts")
    @Builder.Default
    private Integer verificationCodeAttempts = 0;

    /** true cuando la contraseña actual es temporal y el usuario debe cambiarla. */
    @Column(name = "must_change_password")
    @Builder.Default
    private Boolean mustChangePassword = false;

    /** Fecha de vencimiento de la contraseña temporal (admin o recordatorio). */
    @Column(name = "temp_password_expires_at")
    private LocalDateTime temporaryPasswordExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}