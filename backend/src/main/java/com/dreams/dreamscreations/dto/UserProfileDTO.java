package com.dreams.dreamscreations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String phone;
    private String profilePhotoUrl;
    private boolean emailVerified;
    private boolean profileComplete;
    private Long customerId;
    private Long supervisorId;
    private String address;
    private String city;
}
