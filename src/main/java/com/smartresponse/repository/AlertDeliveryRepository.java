package com.smartresponse.repository;
import com.smartresponse.domain.AlertDelivery; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AlertDeliveryRepository extends JpaRepository<AlertDelivery,UUID> {
 boolean existsByEmergency_IdAndRecipient_Id(UUID emergencyId, UUID recipientId);
 java.util.List<AlertDelivery> findByRecipient_IdOrderByCreatedAtDesc(UUID recipientId);
 java.util.List<AlertDelivery> findByRecipient_IdAndEmergency_ReporterIdNotOrderByCreatedAtDesc(UUID recipientId, UUID reporterId);
}
