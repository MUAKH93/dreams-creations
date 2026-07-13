package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.UpdateProfileRequest;
import com.dreams.dreamscreations.dto.UserProfileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    UserProfileDTO getMyProfile();
    UserProfileDTO updateMyProfile(UpdateProfileRequest request);
    UserProfileDTO uploadPhoto(MultipartFile file);
}
