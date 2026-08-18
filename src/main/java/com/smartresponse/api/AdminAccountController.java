package com.smartresponse.api;

import com.smartresponse.repository.RoleApplicationRepository;
import com.smartresponse.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/accounts")
public class AdminAccountController {
  private final UserRepository users;
  private final RoleApplicationRepository applications;
  private final com.smartresponse.repository.VerificationDocumentRepository documents;

  public AdminAccountController(UserRepository users, RoleApplicationRepository applications,
                                com.smartresponse.repository.VerificationDocumentRepository documents) {
    this.users = users;
    this.applications = applications;
    this.documents = documents;
  }

  @GetMapping
  public List<Map<String, Object>> accounts() {
    return users.findAll().stream().map(user -> {
      Map<String, Object> account = new java.util.LinkedHashMap<>();
      account.put("name", user.getFullName());
      account.put("email", user.getEmail());
      account.put("roles", user.getRoles());
      account.put("emailVerified", user.isEmailVerified());
      account.put("phoneVerified", user.isPhoneVerified());
      applications.findFirstByUser_EmailIgnoreCaseOrderBySubmittedAtDesc(user.getEmail()).ifPresent(application -> {
        account.put("applicationStatus", application.getReviewStatus());
        account.put("requestedRole", application.getRequestedRole());
        account.put("documentCount", documents.countByApplicationId(application.getId()));
      });
      return account;
    }).toList();
  }
}
