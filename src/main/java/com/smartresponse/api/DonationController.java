package com.smartresponse.api;
import com.smartresponse.domain.*; import com.smartresponse.repository.DonationRepository; import com.smartresponse.repository.UserRepository;
import com.smartresponse.service.CommunityNotificationService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/donations")
public class DonationController {
 private final DonationRepository donations; private final CommunityNotificationService notifications; private final UserRepository users;
 public DonationController(DonationRepository donations, CommunityNotificationService notifications, UserRepository users) { this.donations=donations; this.notifications=notifications; this.users=users; }
 @PostMapping public ResponseEntity<Donation> pledge(@Valid @RequestBody DonationRequest request, Authentication auth) {
  UUID rid = auth!=null ? users.findByEmailIgnoreCase(auth.getName()).map(u->u.getId()).orElse(null) : null;
  Donation d=donations.save(new Donation(request.donationType(),request.amount(),request.currency(),request.itemDescription(),request.targetName(),request.targetType(),rid));
  notifications.notifyRoles("TARGETED DONATION",request.donationType()+" offered for "+request.targetType()+": "+request.targetName()+". "+request.itemDescription(),null,null,EnumSet.of(Role.NGO,Role.DISPATCHER),request.targetName());
  return ResponseEntity.status(HttpStatus.CREATED).body(d);
 }
 @GetMapping public List<Donation> list() { return donations.findByDonationStatusOrderByCreatedAtDesc("PLEDGED"); }
 @GetMapping("/mine") public List<Donation> mine(Authentication auth) { UUID rid=users.findByEmailIgnoreCase(auth.getName()).map(u->u.getId()).orElseThrow(); return donations.findByReporterIdOrderByCreatedAtDesc(rid); }
}
