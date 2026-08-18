package com.smartresponse.repository;
import com.smartresponse.domain.AppUser; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserRepository extends JpaRepository<AppUser, UUID> { Optional<AppUser> findByEmailIgnoreCase(String email); Optional<AppUser> findByPhoneHash(String phoneHash); }
