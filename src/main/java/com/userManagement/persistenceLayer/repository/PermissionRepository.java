package com.userManagement.persistenceLayer.repository;

import com.userManagement.persistenceLayer.entity.Permission;
import com.userManagement.persistenceLayer.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}