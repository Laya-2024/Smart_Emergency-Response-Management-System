package com.smartresponse.domain;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name="password_reset_tokens", indexes=@Index(name="idx_reset_token",columnList="tokenHash",unique=true))
public class PasswordResetToken {
 @Id @GeneratedValue private UUID id; @ManyToOne(optional=false, fetch=FetchType.LAZY) private AppUser user;
 @Column(nullable=false, unique=true, length=64) private String tokenHash; @Column(nullable=false) private Instant expiresAt; private Instant usedAt;
 protected PasswordResetToken(){} public PasswordResetToken(AppUser user,String hash){this.user=user;tokenHash=hash;expiresAt=Instant.now().plus(Duration.ofMinutes(20));}
 public AppUser getUser(){return user;} public String getTokenHash(){return tokenHash;} public boolean valid(){return usedAt==null && expiresAt.isAfter(Instant.now());} public void use(){usedAt=Instant.now();}
}
