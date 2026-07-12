package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.admin.*;

import java.util.List;

public interface StaffAccountService {
    List<SupervisorAccountDTO> listSupervisorAccounts();
    SupervisorAccountDTO createSupervisorWithAccount(CreateSupervisorAccountRequest request);
    SupervisorAccountDTO createLoginForSupervisor(Long supervisorId, CreateLoginForSupervisorRequest request);
    SupervisorAccountDTO updateSupervisor(Long supervisorId, UpdateSupervisorAccountRequest request);
    SupervisorAccountDTO updateSupervisorLogin(Long supervisorId, UpdateStaffLoginRequest request);
    void deleteSupervisor(Long supervisorId, Long actingAdminUserId);

    List<ManagerAccountDTO> listManagerAccounts();
    ManagerAccountDTO createManagerAccount(CreateManagerAccountRequest request);
    ManagerAccountDTO updateManagerAccount(Long userId, UpdateManagerAccountRequest request, Long actingAdminUserId);
    void deleteManagerAccount(Long userId, Long actingAdminUserId);
}
