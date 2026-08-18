package com.smartresponse.repository;

import com.smartresponse.domain.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, UUID> {
  List<BloodRequest> findByRequestStatusOrderByCreatedAtDesc(String requestStatus);
  List<BloodRequest> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);
}
