package com.smartresponse.repository;

import com.smartresponse.domain.VerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, UUID> {
  boolean existsByApplicationIdAndDocumentType(UUID applicationId, String documentType);
  long countByApplicationId(UUID applicationId);
}
