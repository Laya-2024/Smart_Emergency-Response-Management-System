package com.smartresponse.repository;

import com.smartresponse.domain.EmergencyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmergencyAssignmentRepository extends JpaRepository<EmergencyAssignment, UUID> {
  boolean existsByEmergencyId(UUID emergencyId);
  boolean existsByEmergencyIdAndResponder_User_Id(UUID emergencyId, UUID responderUserId);
}
