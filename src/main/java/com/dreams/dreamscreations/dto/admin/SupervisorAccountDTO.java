package com.dreams.dreamscreations.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupervisorAccountDTO {
    private Long supervisorId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String status;
    private boolean hasLogin;
    private Long userId;
    private String username;
}
