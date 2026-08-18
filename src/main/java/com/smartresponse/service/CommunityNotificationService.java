package com.smartresponse.service;

import com.smartresponse.domain.ResponderProfile;
import com.smartresponse.domain.Role;
import com.smartresponse.repository.ResponderProfileRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CommunityNotificationService {
 private final ResponderProfileRepository responders;
 private final RealtimeAlertHub hub;
 private final JavaMailSender mail;
 public CommunityNotificationService(ResponderProfileRepository responders, RealtimeAlertHub hub, JavaMailSender mail) { this.responders = responders; this.hub = hub; this.mail = mail; }

 public void notifyRoles(String type, String details, Double latitude, Double longitude, Set<Role> roles) {
  notifyRoles(type, details, latitude, longitude, roles, null);
 }

 public void notifyRoles(String type, String details, Double latitude, Double longitude, Set<Role> roles, String targetOrganisation) {
  for (ResponderProfile profile : responders.findByVerificationStatus("APPROVED")) {
   if (profile.getUser().getRoles().stream().noneMatch(roles::contains) || !near(profile, latitude, longitude)) continue;
   if (targetOrganisation != null && (profile.getOrganisationName() == null || !profile.getOrganisationName().equalsIgnoreCase(targetOrganisation.trim()))) continue;
   Map<String, Object> event = Map.of("emergencyId", UUID.randomUUID(), "type", type, "latitude", latitude == null ? 0D : latitude, "longitude", longitude == null ? 0D : longitude, "description", details == null ? "No details provided." : details, "createdAt", Instant.now().toString(), "actionable", false);
   hub.send(profile.getUser().getId(), event);
   sendEmail(profile.getUser().getEmail(), type, details);
  }
 }

 private boolean near(ResponderProfile profile, Double latitude, Double longitude) {
  if (latitude == null || longitude == null) return true;
  if (profile.getLatitude() == null || profile.getLongitude() == null) return false;
  double radius=6371, dLat=Math.toRadians(profile.getLatitude()-latitude), dLon=Math.toRadians(profile.getLongitude()-longitude);
  double a=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(Math.toRadians(latitude))*Math.cos(Math.toRadians(profile.getLatitude()))*Math.sin(dLon/2)*Math.sin(dLon/2);
  return radius*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a)) <= 5;
 }
 private void sendEmail(String recipient, String type, String details) { try { SimpleMailMessage message=new SimpleMailMessage(); message.setTo(recipient); message.setSubject("SafeLink notification: " + type); message.setText("A new " + type + " request needs attention.\n\nDetails: " + (details == null ? "Not provided" : details) + "\n\nOpen SafeLink responder alerts to view it."); mail.send(message); } catch (Exception ignored) { } }
}
