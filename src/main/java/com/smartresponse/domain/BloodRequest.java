package com.smartresponse.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {
  @Id @GeneratedValue private UUID id;
  private String bloodGroup;
  private int unitsRequired;
  private String hospitalName;
  private String contactNote;
  private double latitude;
  private double longitude;
  private String requestStatus = "OPEN";
  @Column(name="reporter_id") private UUID reporterId;
  private Instant createdAt = Instant.now();
  protected BloodRequest() { }
  public BloodRequest(String bloodGroup, int unitsRequired, String hospitalName, String contactNote, double latitude, double longitude, UUID reporterId) { this.bloodGroup=bloodGroup; this.unitsRequired=unitsRequired; this.hospitalName=hospitalName; this.contactNote=contactNote; this.latitude=latitude; this.longitude=longitude; this.reporterId=reporterId; }
  public UUID getId(){ return id; } public String getBloodGroup(){ return bloodGroup; } public int getUnitsRequired(){ return unitsRequired; } public String getHospitalName(){ return hospitalName; } public String getRequestStatus(){ return requestStatus; } public UUID getReporterId(){ return reporterId; } public Instant getCreatedAt(){ return createdAt; }
}
