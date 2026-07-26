package com.dreams.dreamscreations.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerAccountDTO {
    private Long userId;
    private String username;
    private String email;
    private Boolean loginEnabled;
}
