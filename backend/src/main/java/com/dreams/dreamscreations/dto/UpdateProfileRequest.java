package com.dreams.dreamscreations.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private String city;
}
