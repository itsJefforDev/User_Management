package com.userManagement.presentationLayer.controller;

import com.userManagement.businessLayer.dto.RolePermissionDTOs.*;
import com.userManagement.businessLayer.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(@RequestBody CreatePermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }
}