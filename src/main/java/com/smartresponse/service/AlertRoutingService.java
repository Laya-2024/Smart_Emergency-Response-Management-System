package com.smartresponse.service;

import com.smartresponse.domain.AlertDelivery;
import com.smartresponse.domain.Emergency;
import com.smartresponse.domain.EmergencyType;
import com.smartresponse.domain.Role;
import com.smartresponse.repository.AlertDeliveryRepository;
import com.smartresponse.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AlertRoutingService {
 private final UserRepository users;
 private final AlertDeliveryRepository deliveries;
 private final RealtimeAlertHub hub;
 private final JavaMailSender mail;

 public AlertRoutingService(UserRepository users, AlertDeliveryRepository deliveries, RealtimeAlertHub hub, JavaMailSender mail) {
  this.users = users;
  this.deliveries = deliveries;
  this.hub = hub;
  this.mail = mail;
 }

 @Transactional public void route(Emergency emergency) { route(emergency, 5); }

 @Transactional public void route(Emergency emergency, double radiusKm) {
  for (var recipient : users.findAll()) {
   // Every other active account gets an actionable alert. The creator never
   // receives their own emergency back as a responder alert.
   if (emergency.getReporterId().equals(recipient.getId()) || !recipient.isEnabled()
     || deliveries.existsByEmergency_IdAndRecipient_Id(emergency.getId(), recipient.getId())) continue;
   AlertDelivery delivery = deliveries.save(new AlertDelivery(emergency, recipient));
   Map<String, Object> event = Map.of("emergencyId", emergency.getId(), "type", emergency.getType(), "latitude", emergency.getLatitude(), "longitude", emergency.getLongitude(), "description", Objects.toString(emergency.getDescription(), ""), "message", "A verified emergency needs response nearby.");
   try {
     hub.send(delivery.getRecipientId(), event); // in-app real-time notification
   } catch (Exception ignored) {
     // A disconnected browser stream must never interrupt emergency routing.
   }
   sendEmergencyEmail(recipient.getEmail(), emergency); // email notification
  }
 }

 private void sendEmergencyEmail(String recipient, Emergency emergency) {
  try {
   SimpleMailMessage message = new SimpleMailMessage();
   message.setTo(recipient);
   message.setSubject("SafeLink emergency alert: " + emergency.getType());
   message.setText("A nearby emergency needs response.\n\nType: " + emergency.getType()
     + "\nLocation: " + emergency.getLatitude() + ", " + emergency.getLongitude()
     + "\nDetails: " + Objects.toString(emergency.getDescription(), "Not provided")
     + "\n\nOpen your SafeLink responder dashboard to accept the response.");
   mail.send(message);
  } catch (Exception ignored) {
   // A mail outage must never stop the real-time delivery or emergency routing.
  }
 }

}
