package com.smartresponse.repository;
import com.smartresponse.domain.DisasterEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DisasterEventRepository extends JpaRepository<DisasterEvent, UUID> { List<DisasterEvent> findByStatusOrderByStartedAtDesc(String status); }
