package com.dreams.dreamscreations.dto.admin;

import lombok.Data;

@Data
public class CreateManagerAccountRequest {
    private String username;
    private String email;
    private String password;
}
