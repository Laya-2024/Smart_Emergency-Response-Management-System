package com.smartresponse.repository;
import com.smartresponse.domain.ResponderProfile; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ResponderProfileRepository extends JpaRepository<ResponderProfile,UUID> { List<ResponderProfile> findByVerificationStatusAndAvailabilityStatus(String verificationStatus,String availabilityStatus); List<ResponderProfile> findByVerificationStatus(String verificationStatus); Optional<ResponderProfile> findByUserId(UUID userId); }
