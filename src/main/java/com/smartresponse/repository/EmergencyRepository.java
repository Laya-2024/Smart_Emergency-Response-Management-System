package com.smartresponse.repository;
import com.smartresponse.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.Instant;
public interface EmergencyRepository extends JpaRepository<Emergency, UUID> {
    Optional<Emergency> findByIdempotencyKey(String idempotencyKey);
    Page<Emergency> findByStatus(EmergencyStatus status, Pageable pageable);
    List<Emergency> findByStatusAndCreatedAtBefore(EmergencyStatus status, Instant before);
    long countByStatus(EmergencyStatus status);
    List<Emergency> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);
}
