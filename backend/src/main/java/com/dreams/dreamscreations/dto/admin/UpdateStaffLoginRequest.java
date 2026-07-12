package com.dreams.dreamscreations.dto.admin;

import lombok.Data;

@Data
public class UpdateStaffLoginRequest {
    private String username;
    private String email;
    private String password;
    private Boolean loginEnabled;
}
