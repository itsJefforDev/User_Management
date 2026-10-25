package com.userManagement.businessLayer.service;

import com.userManagement.businessLayer.dto.AuditDTOs.*;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {
    void logEvent(CreateAuditLogRequest request);
    List<AuditLogResponse> getAllLogs();
    List<AuditLogResponse> getLogsWithFilters(String user, String module, String action, LocalDateTime startDate, LocalDateTime endDate);
}