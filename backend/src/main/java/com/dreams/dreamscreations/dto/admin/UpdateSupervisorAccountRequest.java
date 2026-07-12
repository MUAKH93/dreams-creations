package com.dreams.dreamscreations.dto.admin;

import lombok.Data;

@Data
public class UpdateSupervisorAccountRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String status;
}
