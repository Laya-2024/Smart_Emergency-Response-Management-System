package com.smartresponse.repository;
import com.smartresponse.domain.RoleApplication; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface RoleApplicationRepository extends JpaRepository<RoleApplication,UUID>{
 List<RoleApplication> findByReviewStatusOrderBySubmittedAtAsc(String reviewStatus);
 Optional<RoleApplication> findFirstByUser_EmailIgnoreCaseOrderBySubmittedAtDesc(String email);
}
