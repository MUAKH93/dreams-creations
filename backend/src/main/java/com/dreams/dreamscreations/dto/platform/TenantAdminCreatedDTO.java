package com.dreams.dreamscreations.dto.platform;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantAdminCreatedDTO {
    private String tenantId;
    private Long userId;
    private String username;
    private String email;
    private String role;
}
