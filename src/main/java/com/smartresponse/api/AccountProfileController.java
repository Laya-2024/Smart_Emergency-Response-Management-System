package com.smartresponse.api;

import com.smartresponse.repository.UserRepository;
import com.smartresponse.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/account")
public class AccountProfileController {
    private final UserRepository users; private final AccountService accounts;

    public AccountProfileController(UserRepository users, AccountService accounts) { this.users = users; this.accounts = accounts; }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        var user = users.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
        return Map.of("fullName", user.getFullName(), "email", user.getEmail(), "roles", user.getRoles(), "emailVerified", user.isEmailVerified(), "phoneVerified", user.isPhoneVerified());
    }
    @org.springframework.web.bind.annotation.PostMapping("/password")
    public Map<String,String> password(Authentication authentication,@Valid @org.springframework.web.bind.annotation.RequestBody ChangePasswordRequest request) { accounts.changePassword(authentication.getName(),request.currentPassword(),request.newPassword()); return Map.of("message","Password changed successfully."); }
}
