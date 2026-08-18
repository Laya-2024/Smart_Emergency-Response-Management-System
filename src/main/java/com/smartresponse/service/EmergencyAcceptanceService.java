package com.smartresponse.service;

import com.smartresponse.domain.AppUser;
import com.smartresponse.domain.Emergency;
import com.smartresponse.domain.EmergencyAssignment;
import com.smartresponse.domain.EmergencyStatus;
import com.smartresponse.domain.ResponderProfile;
import com.smartresponse.repository.EmergencyAssignmentRepository;
import com.smartresponse.repository.AlertDeliveryRepository;
import com.smartresponse.repository.EmergencyRepository;
import com.smartresponse.repository.ResponderProfileRepository;
import com.smartresponse.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
public class EmergencyAcceptanceService {
  private final EmergencyRepository emergencies;
  private final ResponderProfileRepository responders;
 private final EmergencyAssignmentRepository assignments;
 private final AlertDeliveryRepository deliveries;
 private final UserRepository users;
 private final RealtimeAlertHub hub;

 public EmergencyAcceptanceService(EmergencyRepository emergencies, ResponderProfileRepository responders,
                                    EmergencyAssignmentRepository assignments, UserRepository users, AlertDeliveryRepository deliveries, RealtimeAlertHub hub) {
    this.emergencies = emergencies;
    this.responders = responders;
    this.assignments = assignments;
    this.users = users;
    this.deliveries = deliveries;
    this.hub = hub;
  }

  @Transactional
  public UUID accept(UUID emergencyId, String email) {
    AppUser user = users.findByEmailIgnoreCase(email).orElseThrow();
    ResponderProfile responder = responders.findByUserId(user.getId())
        .orElseGet(() -> responders.save(new ResponderProfile(user, "COMMUNITY")));
    if (assignments.existsByEmergencyIdAndResponder_User_Id(emergencyId, user.getId())) {
      throw new IllegalStateException("You already accepted this emergency");
    }

    Emergency emergency = emergencies.findById(emergencyId).orElseThrow();
    if (emergency.getReporterId().equals(user.getId())) {
      throw new SecurityException("You cannot accept your own emergency");
    }
    if (!deliveries.existsByEmergency_IdAndRecipient_Id(emergencyId, user.getId())) {
      throw new SecurityException("This emergency was not routed to your responder account");
    }
    if (emergency.getStatus() == EmergencyStatus.OPEN) emergency.acknowledge();
    if (assignments.existsByEmergencyId(emergencyId)) {
      throw new IllegalStateException("Another responder has already accepted this emergency");
    }
    if (emergency.getStatus() != EmergencyStatus.ACKNOWLEDGED && emergency.getStatus() != EmergencyStatus.IN_PROGRESS) {
      throw new IllegalStateException("This emergency is no longer available");
    }
    UUID assignmentId = assignments.save(new EmergencyAssignment(emergency, responder)).getId();
    Map<String, Object> update = new HashMap<>();
    update.put("eventType", "EMERGENCY_STATUS");
    update.put("emergencyId", emergencyId);
    update.put("status", EmergencyStatus.ACKNOWLEDGED.name());
    update.put("responderName", user.getFullName());
    update.put("responderLatitude", responder.getLatitude());
    update.put("responderLongitude", responder.getLongitude());
    update.put("message", user.getFullName() + " has accepted your request and is on the way to help.");
    hub.send(emergency.getReporterId(), update);
    return assignmentId;
  }
}
