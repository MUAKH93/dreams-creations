package com.dreams.dreamscreations.dto.admin;

import lombok.Data;

@Data
public class CreateLoginForSupervisorRequest {
    private String username;
    private String password;
    /** Optional — updates supervisor email if missing or mismatched */
    private String email;
}
