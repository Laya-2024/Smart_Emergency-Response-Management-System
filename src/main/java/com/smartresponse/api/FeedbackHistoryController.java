package com.smartresponse.api;
import com.smartresponse.domain.EmergencyFeedback;
import com.smartresponse.repository.EmergencyFeedbackRepository;
import com.smartresponse.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackHistoryController {
 private final EmergencyFeedbackRepository feedback; private final UserRepository users;
 public FeedbackHistoryController(EmergencyFeedbackRepository feedback, UserRepository users){this.feedback=feedback;this.users=users;}
 @GetMapping("/mine") public List<FeedbackResponse> mine(Authentication auth){UUID id=users.findByEmailIgnoreCase(auth.getName()).orElseThrow().getId();return feedback.findByReporter_IdOrderByCreatedAtDesc(id).stream().map(this::map).toList();}
 @GetMapping public List<FeedbackResponse> all(){return feedback.findAllByOrderByCreatedAtDesc().stream().map(this::map).toList();}
 private FeedbackResponse map(EmergencyFeedback f){return new FeedbackResponse(f.getId(),f.getEmergency().getId(),f.getEmergency().getType().name(),f.getRating(),f.getComments(),f.getCreatedAt(),f.getReporter().getFullName());}
}
