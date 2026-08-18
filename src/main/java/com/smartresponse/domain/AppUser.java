package com.smartresponse.domain;

import jakarta.persistence.*; import java.time.Instant; import java.util.*;
@Entity @Table(name="app_users", indexes=@Index(name="idx_user_email", columnList="email", unique=true))
public class AppUser {
 @Id @GeneratedValue private UUID id;
 @Column(nullable=false, unique=true, length=254) private String email;
 @Column(nullable=false, length=100) private String fullName;
 @Column(nullable=false, length=100) private String passwordHash;
 @ElementCollection(targetClass=Role.class, fetch=FetchType.EAGER) @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id")) @Enumerated(EnumType.STRING) @Column(name="role", nullable=false) private Set<Role> roles = new HashSet<>();
 @Column(nullable=false) private boolean enabled = true;
 @Column(name="phone_encrypted") private byte[] phoneEncrypted;
 @Column(name="phone_hash", length=64, columnDefinition="char(64)") private String phoneHash;
 @Column(name="email_verified", nullable=false) private boolean emailVerified;
 @Column(name="phone_verified", nullable=false) private boolean phoneVerified;
 @Column(nullable=false, updatable=false) private Instant createdAt = Instant.now();
 protected AppUser() { }
 public AppUser(String email, String fullName, String passwordHash, Set<Role> roles, byte[] phone, String phoneHash) { this.email=email; this.fullName=fullName; this.passwordHash=passwordHash; this.roles=roles; this.phoneEncrypted=phone; this.phoneHash=phoneHash; }
 public UUID getId(){return id;} public String getEmail(){return email;} public String getFullName(){return fullName;} public String getPasswordHash(){return passwordHash;} public Set<Role> getRoles(){return roles;} public boolean isEmailVerified(){return emailVerified;} public boolean isPhoneVerified(){return phoneVerified;} public boolean isEnabled(){return enabled&&emailVerified&&phoneVerified;} public void verifyEmail(){emailVerified=true;} public void verifyPhone(){phoneVerified=true;}
 public void approveRole(Role role){roles.add(role);}
 public void changePassword(String hash){passwordHash=hash;}
}
