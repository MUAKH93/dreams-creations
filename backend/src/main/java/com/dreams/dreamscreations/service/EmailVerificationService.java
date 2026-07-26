package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.auth.RegisterResponse;
import com.dreams.dreamscreations.entity.User;

public interface EmailVerificationService {
    RegisterResponse sendVerificationForUser(User user);
    void verify(String token);
    RegisterResponse resend(String email);
}
