package com.smartresponse.api;

import com.smartresponse.domain.ReliefRequest;
import com.smartresponse.domain.Role;
import com.smartresponse.domain.Shelter;
import com.smartresponse.repository.ReliefRequestRepository;
import com.smartresponse.repository.ShelterRepository;
import com.smartresponse.repository.UserRepository;
import com.smartresponse.service.CommunityNotificationService;
import com.smartresponse.service.TrustedContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OperationsController {
 private final ShelterRepository shelters;
 private final ReliefRequestRepository relief;
 private final CommunityNotificationService notifications;
 private final UserRepository users;
 private final TrustedContactService trustedContacts;

 public OperationsController(ShelterRepository shelters, ReliefRequestRepository relief,
     CommunityNotificationService notifications, UserRepository users, TrustedContactService trustedContacts) {
  this.shelters=shelters; this.relief=relief; this.notifications=notifications;
  this.users=users; this.trustedContacts=trustedContacts;
 }

 @GetMapping("/shelters") public List<Shelter> shelters(){ return shelters.findByStatusOrderByCapacityAvailableDesc("OPEN"); }
 @PostMapping("/shelters") public ResponseEntity<Shelter> addShelter(@Valid @RequestBody ShelterRequest request){ return ResponseEntity.status(HttpStatus.CREATED).body(shelters.save(new Shelter(request.name(),request.addressLine(),request.latitude(),request.longitude(),request.capacityTotal()))); }
 @PatchMapping("/shelters/{id}/capacity") public Shelter capacity(@PathVariable UUID id,@RequestParam int available){ Shelter shelter=shelters.findById(id).orElseThrow(); shelter.setCapacity(available); return shelter; }

 @PostMapping("/relief-requests")
 public ResponseEntity<ReliefRequest> createRelief(@Valid @RequestBody ReliefRequestBody request, Authentication auth) {
  UUID reporterId=auth==null?null:users.findByEmailIgnoreCase(auth.getName()).map(u->u.getId()).orElse(null);
  ReliefRequest saved=relief.save(new ReliefRequest(request.requestType(),request.peopleCount(),request.description(),request.latitude(),request.longitude(),reporterId));
  String details=request.requestType()+" for "+request.peopleCount()+" people. "+request.description();
  notifications.notifyRoles("RELIEF REQUEST",details,request.latitude(),request.longitude(),EnumSet.of(Role.NGO,Role.SHELTER_MANAGER,Role.VOLUNTEER,Role.DISPATCHER));
  if(reporterId!=null) trustedContacts.notifyRequest(reporterId,"relief request",details);
  return ResponseEntity.status(HttpStatus.CREATED).body(saved);
 }

 @GetMapping("/relief-requests") public List<ReliefRequest> relief(){ return relief.findByRequestStatusOrderByCreatedAtDesc("OPEN"); }
 @GetMapping("/relief-requests/mine") public List<ReliefRequest> myRelief(Authentication auth){ UUID reporterId=users.findByEmailIgnoreCase(auth.getName()).map(u->u.getId()).orElseThrow(); return relief.findByReporterIdOrderByCreatedAtDesc(reporterId); }
}
