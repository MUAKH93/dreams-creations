package com.dreams.dreamscreations.security;

import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.CustomerRepository;
import com.dreams.dreamscreations.repository.SupervisorRepository;
import com.dreams.dreamscreations.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final SupervisorRepository supervisorRepo;

    public CurrentUserService(UserRepository userRepo,
                              CustomerRepository customerRepo,
                              SupervisorRepository supervisorRepo) {
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.supervisorRepo = supervisorRepo;
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails details) {
            return userRepo.findFirstByUsername(details.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("Not authenticated");
    }

    public Long resolveCustomerId(User user) {
        if (user.getEmail() == null) return null;
        return customerRepo.findFirstByEmail(user.getEmail())
                .map(c -> c.getCustomerId())
                .orElse(null);
    }

    public Long resolveSupervisorId(User user) {
        if (user.getEmail() == null) return null;
        return supervisorRepo.findFirstByEmail(user.getEmail().trim())
                .map(s -> s.getSupervisorId())
                .orElse(null);
    }

    public Long requireCustomerId() {
        Long id = resolveCustomerId(getCurrentUser());
        if (id == null) {
            throw new RuntimeException("No customer profile linked to this account. Contact support.");
        }
        return id;
    }

    public Long requireSupervisorId() {
        Long id = resolveSupervisorId(getCurrentUser());
        if (id == null) {
            throw new RuntimeException("No supervisor profile linked to this account. Ensure your user email matches the supervisor record.");
        }
        return id;
    }
}
