package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.admin.CreateLoginForSupervisorRequest;
import com.dreams.dreamscreations.dto.admin.CreateSupervisorAccountRequest;
import com.dreams.dreamscreations.dto.admin.SupervisorAccountDTO;
import com.dreams.dreamscreations.service.StaffAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/supervisor-accounts")
public class AdminStaffController {

    private final StaffAccountService service;

    public AdminStaffController(StaffAccountService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SupervisorAccountDTO>> list() {
        return ResponseEntity.ok(service.listSupervisorAccounts());
    }

    @PostMapping
    public ResponseEntity<SupervisorAccountDTO> create(@RequestBody CreateSupervisorAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createSupervisorWithAccount(request));
    }

    @PostMapping("/{supervisorId}/login")
    public ResponseEntity<SupervisorAccountDTO> createLogin(
            @PathVariable Long supervisorId,
            @RequestBody CreateLoginForSupervisorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createLoginForSupervisor(supervisorId, request));
    }
}
