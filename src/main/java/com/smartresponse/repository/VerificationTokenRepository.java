package com.smartresponse.repository;
import com.smartresponse.domain.VerificationToken; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface VerificationTokenRepository extends JpaRepository<VerificationToken,UUID> { List<VerificationToken> findByTokenHash(String tokenHash); }
