package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.auth.TenantPublicDTO;
import com.dreams.dreamscreations.dto.platform.CreateTenantAdminRequest;
import com.dreams.dreamscreations.dto.platform.TenantAdminCreatedDTO;
import com.dreams.dreamscreations.service.PlatformTenantAdminService;
import com.dreams.dreamscreations.tenant.TenantRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform")
public class PlatformController {

    private final TenantRegistry tenantRegistry;
    private final PlatformTenantAdminService tenantAdminService;

    public PlatformController(TenantRegistry tenantRegistry,
                              PlatformTenantAdminService tenantAdminService) {
        this.tenantRegistry = tenantRegistry;
        this.tenantAdminService = tenantAdminService;
    }

    @GetMapping("/tenants")
    public ResponseEntity<List<TenantPublicDTO>> listTenants() {
        List<TenantPublicDTO> list = tenantRegistry.listPublicTenants().stream()
                .map(t -> new TenantPublicDTO(t.id(), t.displayName()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/tenant-admins")
    public ResponseEntity<TenantAdminCreatedDTO> createTenantAdmin(
            @RequestBody CreateTenantAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantAdminService.createTenantAdmin(request));
    }
}
