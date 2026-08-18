package com.smartresponse.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "disaster_events")
public class DisasterEvent {
 @Id @GeneratedValue private UUID id;
 private String eventType;
 private String title;
 private String severity;
 private String status = "ACTIVE";
 private String affectedArea;
 private Instant startedAt = Instant.now();
 private Instant endedAt;
 protected DisasterEvent() { }
 public DisasterEvent(String type, String title, String severity, String area) { eventType=type; this.title=title; this.severity=severity; affectedArea=area; }
 public UUID getId(){return id;} public String getEventType(){return eventType;} public String getTitle(){return title;} public String getSeverity(){return severity;} public String getStatus(){return status;} public String getAffectedArea(){return affectedArea;}
 public void close(){status="CLOSED";endedAt=Instant.now();}
}
