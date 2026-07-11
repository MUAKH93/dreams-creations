package com.dreams.dreamscreations.dto.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    // Customers self-register — role is always assigned as CUSTOMER
    // Staff accounts are created by admin only
}
