package com.smartresponse.repository;
import com.smartresponse.domain.TrustedContact; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TrustedContactRepository extends JpaRepository<TrustedContact,UUID>{List<TrustedContact> findByOwnerIdAndActiveTrue(UUID ownerId);Optional<TrustedContact> findByIdAndOwnerId(UUID id,UUID ownerId);}
