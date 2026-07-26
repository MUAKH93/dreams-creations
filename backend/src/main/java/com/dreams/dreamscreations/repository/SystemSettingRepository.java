package com.dreams.dreamscreations.repository;

import com.dreams.dreamscreations.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
