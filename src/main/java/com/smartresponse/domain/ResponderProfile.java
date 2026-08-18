package com.smartresponse.domain;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="responder_profiles") public class ResponderProfile {
 @Id @GeneratedValue private UUID id; @OneToOne(fetch=FetchType.EAGER) @JoinColumn(name="user_id",nullable=false) private AppUser user;
 private String organisationName; private String verificationStatus; private String availabilityStatus; private String serviceType;
 private Double latitude; private Double longitude; private Instant updatedAt;
 protected ResponderProfile(){} public ResponderProfile(AppUser user,String service){this.user=user;serviceType=service;verificationStatus="APPROVED";availabilityStatus="OFFLINE";updatedAt=Instant.now();} public AppUser getUser(){return user;} public String getOrganisationName(){return organisationName;} public String getVerificationStatus(){return verificationStatus;} public String getAvailabilityStatus(){return availabilityStatus;} public String getServiceType(){return serviceType;} public Double getLatitude(){return latitude;} public Double getLongitude(){return longitude;} public void setOrganisationName(String organisationName){this.organisationName=organisationName;} public void update(String availability,Double lat,Double lon){availabilityStatus=availability;latitude=lat;longitude=lon;updatedAt=Instant.now();}
}
