package com.dreams.dreamscreations.dto.admin;

import lombok.Data;

@Data
public class CreateSupervisorAccountRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String username;
    private String password;
}
