package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.admin.*;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.StaffAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminStaffController {

    private final StaffAccountService service;
    private final CurrentUserService currentUserService;

    public AdminStaffController(StaffAccountService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    // --- Supervisors ---

    @GetMapping("/supervisor-accounts")
    public ResponseEntity<List<SupervisorAccountDTO>> listSupervisors() {
        return ResponseEntity.ok(service.listSupervisorAccounts());
    }

    @PostMapping("/supervisor-accounts")
    public ResponseEntity<SupervisorAccountDTO> createSupervisor(@RequestBody CreateSupervisorAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createSupervisorWithAccount(request));
    }

    @PostMapping("/supervisor-accounts/{supervisorId}/login")
    public ResponseEntity<SupervisorAccountDTO> createSupervisorLogin(
            @PathVariable Long supervisorId,
            @RequestBody CreateLoginForSupervisorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createLoginForSupervisor(supervisorId, request));
    }

    @PutMapping("/supervisor-accounts/{supervisorId}")
    public ResponseEntity<SupervisorAccountDTO> updateSupervisor(
            @PathVariable Long supervisorId,
            @RequestBody UpdateSupervisorAccountRequest request) {
        return ResponseEntity.ok(service.updateSupervisor(supervisorId, request));
    }

    @PutMapping("/supervisor-accounts/{supervisorId}/login")
    public ResponseEntity<SupervisorAccountDTO> updateSupervisorLogin(
            @PathVariable Long supervisorId,
            @RequestBody UpdateStaffLoginRequest request) {
        return ResponseEntity.ok(service.updateSupervisorLogin(supervisorId, request));
    }

    @DeleteMapping("/supervisor-accounts/{supervisorId}")
    public ResponseEntity<Void> deleteSupervisor(@PathVariable Long supervisorId) {
        service.deleteSupervisor(supervisorId, currentUserService.getCurrentUser().getUserId());
        return ResponseEntity.noContent().build();
    }

    // --- Managers ---

    @GetMapping("/manager-accounts")
    public ResponseEntity<List<ManagerAccountDTO>> listManagers() {
        return ResponseEntity.ok(service.listManagerAccounts());
    }

    @PostMapping("/manager-accounts")
    public ResponseEntity<ManagerAccountDTO> createManager(@RequestBody CreateManagerAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createManagerAccount(request));
    }

    @PutMapping("/manager-accounts/{userId}")
    public ResponseEntity<ManagerAccountDTO> updateManager(
            @PathVariable Long userId,
            @RequestBody UpdateManagerAccountRequest request) {
        return ResponseEntity.ok(service.updateManagerAccount(
                userId, request, currentUserService.getCurrentUser().getUserId()));
    }

    @DeleteMapping("/manager-accounts/{userId}")
    public ResponseEntity<Void> deleteManager(@PathVariable Long userId) {
        service.deleteManagerAccount(userId, currentUserService.getCurrentUser().getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/staff/{userId}/reset-password")
    public ResponseEntity<Void> resetStaffPassword(
            @PathVariable Long userId,
            @RequestBody ResetStaffPasswordRequest request) {
        service.resetStaffPassword(
                userId,
                request.getNewPassword(),
                currentUserService.getCurrentUser().getUserId());
        return ResponseEntity.noContent().build();
    }
}
