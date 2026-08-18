package com.smartresponse.api;

import com.smartresponse.domain.BloodRequest;
import com.smartresponse.domain.Role;
import com.smartresponse.repository.BloodRequestRepository;
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
@RequestMapping("/api/v1/blood-requests")
public class BloodRequestController {
    private final BloodRequestRepository requests;
    private final CommunityNotificationService notifications;
    private final UserRepository users;
    private final TrustedContactService trustedContacts;

    public BloodRequestController(BloodRequestRepository requests, CommunityNotificationService notifications, UserRepository users, TrustedContactService trustedContacts) {
        this.requests = requests; this.notifications = notifications; this.users = users; this.trustedContacts = trustedContacts;
    }

    @PostMapping
    public ResponseEntity<BloodRequest> create(@Valid @RequestBody BloodRequestBody body, Authentication auth) {
        UUID rid = auth != null ? users.findByEmailIgnoreCase(auth.getName()).map(u -> u.getId()).orElse(null) : null;
        BloodRequest r = requests.save(new BloodRequest(body.bloodGroup(), body.unitsRequired(), body.hospitalName(), body.contactNote(), body.latitude(), body.longitude(), rid));
        notifications.notifyRoles("BLOOD REQUEST", body.bloodGroup() + " · " + body.unitsRequired() + " unit(s) · " + body.hospitalName() + ". " + body.contactNote(), body.latitude(), body.longitude(), EnumSet.of(Role.DOCTOR, Role.PARAMEDIC, Role.DISPATCHER));
        if (rid != null) trustedContacts.notifyRequest(rid, "blood request", body.bloodGroup() + ": " + body.unitsRequired() + " unit(s) needed at " + body.hospitalName() + ". " + body.contactNote());
        return ResponseEntity.status(HttpStatus.CREATED).body(r);
    }

    @GetMapping
    public List<BloodRequest> list() { return requests.findByRequestStatusOrderByCreatedAtDesc("OPEN"); }

    @GetMapping("/mine")
    public List<BloodRequest> mine(Authentication auth) {
        UUID rid = users.findByEmailIgnoreCase(auth.getName()).map(u -> u.getId()).orElseThrow();
        return requests.findByReporterIdOrderByCreatedAtDesc(rid);
    }
}
