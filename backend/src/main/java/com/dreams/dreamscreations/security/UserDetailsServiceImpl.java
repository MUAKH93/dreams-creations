package com.dreams.dreamscreations.security;

import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional  // keeps session open so LAZY Role can be loaded
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.debug("Attempting to load user: {}", username);

        User user = userRepository.findFirstByUsername(username)
                .orElseThrow(() -> {
                    log.error("User NOT found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.debug("User found: {}, status: {}, passwordHash starts with: {}",
                user.getUsername(),
                user.getStatus(),
                user.getPassword() != null
                        ? user.getPassword().substring(0, Math.min(10, user.getPassword().length()))
                        : "NULL");

        // Access role INSIDE the transaction while session is still open
        String roleName = "CUSTOMER";
        if (user.getRole() != null) {
            roleName = user.getRole().getRoleName();  // triggers LAZY load — safe inside @Transactional
        }
        String role = "ROLE_" + roleName;
        log.debug("Assigned role: {}", role);

        boolean isActive = user.getStatus() != null && user.getStatus();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(role))
                .accountExpired(false)
                .accountLocked(!isActive)
                .credentialsExpired(false)
                .disabled(!isActive)
                .build();
    }
}
