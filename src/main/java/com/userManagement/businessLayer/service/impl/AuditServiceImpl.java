package com.userManagement.businessLayer.service.impl;

import com.userManagement.businessLayer.dto.AuditDTOs.*;
import com.userManagement.businessLayer.service.AuditService;
import com.userManagement.persistenceLayer.entity.AuditLog;
import com.userManagement.persistenceLayer.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logEvent(CreateAuditLogRequest request) {
        AuditLog auditLog = AuditLog.builder()
                .user(request.getUser() != null ? request.getUser() : "SYSTEM")
                .action(request.getAction())
                .module(request.getModule())
                .resource(request.getResource())
                .resourceId(request.getResourceId())
                .timestamp(LocalDateTime.now())
                .result(request.getResult() != null ? request.getResult() : "SUCCESS")
                .ipAddress(request.getIpAddress())
                .additionalInfo(request.getAdditionalInfo())
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogResponse> getLogsWithFilters(String user, String module, String action, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findWithFilters(user, module, action, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .user(log.getUser())
                .action(log.getAction())
                .module(log.getModule())
                .resource(log.getResource())
                .resourceId(log.getResourceId())
                .timestamp(log.getTimestamp())
                .result(log.getResult())
                .ipAddress(log.getIpAddress())
                .additionalInfo(log.getAdditionalInfo())
                .build();
    }
}