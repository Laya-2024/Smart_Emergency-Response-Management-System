package com.smartresponse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresponse.api.ReviewRequest;
import com.smartresponse.api.RoleApplicationRequest;
import com.smartresponse.domain.AppUser;
import com.smartresponse.domain.ResponderProfile;
import com.smartresponse.domain.Role;
import com.smartresponse.domain.RoleApplication;
import com.smartresponse.repository.ResponderProfileRepository;
import com.smartresponse.repository.RoleApplicationRepository;
import com.smartresponse.repository.UserRepository;
import com.smartresponse.repository.VerificationDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoleApplicationService {
  private final RoleApplicationRepository applications;
  private final UserRepository users;
  private final ResponderProfileRepository responders;
  private final VerificationDocumentRepository documents;

  public RoleApplicationService(RoleApplicationRepository applications, UserRepository users,
                                ResponderProfileRepository responders, VerificationDocumentRepository documents) {
    this.applications = applications;
    this.users = users;
    this.responders = responders;
    this.documents = documents;
  }

  @Transactional
  public UUID submit(String email, RoleApplicationRequest request) {
    if (request.requestedRole() == Role.ADMIN) {
      throw new IllegalArgumentException("Administrator accounts do not require verification documents");
    }
    AppUser user = users.findByEmailIgnoreCase(email).orElseThrow();
    return applications.save(new RoleApplication(user, request.requestedRole(), request.organisationName(),
      request.professionalIdMasked(), asJson(request.detailsJson()))).getId();
  }

  @Transactional(readOnly = true)
  public Optional<RoleApplication> mine(String email) {
    return applications.findFirstByUser_EmailIgnoreCaseOrderBySubmittedAtDesc(email);
  }

  @Transactional(readOnly = true)
  public List<RoleApplication> pending() {
    return applications.findByReviewStatusOrderBySubmittedAtAsc("PENDING");
  }

  @Transactional
  public void review(String adminEmail, UUID id, ReviewRequest request) {
    AppUser admin = users.findByEmailIgnoreCase(adminEmail).orElseThrow();
    RoleApplication application = applications.findById(id).orElseThrow();
    boolean publicAccount = application.getRequestedRole() == Role.CITIZEN || application.getRequestedRole() == Role.DONOR;
    boolean hasRequiredDocuments = publicAccount
      ? documents.existsByApplicationIdAndDocumentType(id, "PHOTO") || documents.existsByApplicationIdAndDocumentType(id, "IDENTITY")
      : documents.existsByApplicationIdAndDocumentType(id, "PHOTO") && documents.existsByApplicationIdAndDocumentType(id, "IDENTITY");
    if (request.approved() && !hasRequiredDocuments) {
      throw new IllegalStateException(publicAccount
        ? "One profile photo or masked identity document is required before approval"
        : "A profile photo and masked identity/licence document are required before approval");
    }
    application.review(admin, request.approved(), request.notes());
    if (request.approved()) {
      application.getUser().approveRole(application.getRequestedRole());
      Role role = application.getRequestedRole();
      if (role != Role.ADMIN) {
        ResponderProfile profile = responders.findByUserId(application.getUser().getId())
          .orElseGet(() -> responders.save(new ResponderProfile(application.getUser(), role.name())));
        profile.setOrganisationName(application.getOrganisationName());
      }
    }
  }

  private String asJson(String details) {
    try {
      return new ObjectMapper().readTree(details).toString();
    } catch (Exception ignored) {
      try {
        return new ObjectMapper().writeValueAsString(details.trim());
      } catch (Exception ex) {
        throw new IllegalArgumentException("Service details could not be saved");
      }
    }
  }
}
