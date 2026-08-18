package com.smartresponse.domain;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="alert_deliveries",uniqueConstraints=@UniqueConstraint(name="uk_alert_recipient",columnNames={"emergency_id","recipient_id"})) public class AlertDelivery {
 @Id @GeneratedValue private UUID id; @ManyToOne(optional=false) @JoinColumn(name="emergency_id") private Emergency emergency;
 @ManyToOne(optional=false) @JoinColumn(name="recipient_id") private AppUser recipient;
 private String deliveryChannel; private String deliveryStatus="QUEUED"; private Instant createdAt=Instant.now();
 protected AlertDelivery(){} public AlertDelivery(Emergency e,AppUser u){emergency=e;recipient=u;deliveryChannel="IN_APP";} public UUID getRecipientId(){return recipient.getId();} public Emergency getEmergency(){return emergency;} public Instant getCreatedAt(){return createdAt;}
}
