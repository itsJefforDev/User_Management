package com.userManagement.presentationLayer.controller;

import com.userManagement.businessLayer.dto.AuditDTOs.*;
import com.userManagement.businessLayer.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<AuditLogResponse>> getFilteredAuditLogs(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<AuditLogResponse> logs = auditService.getLogsWithFilters(user, module, action, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
}