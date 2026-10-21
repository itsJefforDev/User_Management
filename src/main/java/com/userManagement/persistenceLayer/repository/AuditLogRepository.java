package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUser(String user);

    List<AuditLog> findByModule(String module);

    List<AuditLog> findByAction(String action);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:user IS NULL OR a.user = :user) AND " +
            "(:module IS NULL OR a.module = :module) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate)")
    List<AuditLog> findWithFilters(
            @Param("user") String user,
            @Param("module") String module,
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}