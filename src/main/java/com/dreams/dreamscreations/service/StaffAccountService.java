package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.admin.CreateLoginForSupervisorRequest;
import com.dreams.dreamscreations.dto.admin.CreateSupervisorAccountRequest;
import com.dreams.dreamscreations.dto.admin.SupervisorAccountDTO;

import java.util.List;

public interface StaffAccountService {
    List<SupervisorAccountDTO> listSupervisorAccounts();
    SupervisorAccountDTO createSupervisorWithAccount(CreateSupervisorAccountRequest request);
    SupervisorAccountDTO createLoginForSupervisor(Long supervisorId, CreateLoginForSupervisorRequest request);
}
