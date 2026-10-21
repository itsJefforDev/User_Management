package com.userManagement.businessLayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class AuditDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuditLogResponse {
        private Long id;
        private String user;
        private String action;
        private String module;
        private String resource;
        private Long resourceId;
        private LocalDateTime timestamp;
        private String result;
        private String ipAddress;
        private String additionalInfo;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateAuditLogRequest {
        private String user;
        private String action;
        private String module;
        private String resource;
        private Long resourceId;
        private String result;
        private String ipAddress;
        private String additionalInfo;
    }
}