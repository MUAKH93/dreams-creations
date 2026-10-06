package com.dreams.dreamscreations.dto.platform;

import lombok.Data;

@Data
public class CreateTenantAdminRequest {
    /** Target tenant / client factory (must match app.multitenancy.tenants.*). */
    private String tenantId;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
}
