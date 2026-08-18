package com.smartresponse.domain;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name="verification_tokens") public class VerificationToken {
 @Id @GeneratedValue private UUID id; @ManyToOne(optional=false) @JoinColumn(name="user_id") private AppUser user; private String channel; private String purpose; @Column(columnDefinition="char(64)") private String tokenHash; private Instant expiresAt; private Instant consumedAt; private int attempts; private Instant createdAt=Instant.now();
 protected VerificationToken(){} public VerificationToken(AppUser u,String channel,String purpose,String hash){user=u;this.channel=channel;this.purpose=purpose;tokenHash=hash;expiresAt=Instant.now().plusSeconds(600);} public boolean valid(String hash){return consumedAt==null&&attempts<5&&expiresAt.isAfter(Instant.now())&&tokenHash.equals(hash);} public void consume(){consumedAt=Instant.now();} public void attempt(){attempts++;} public AppUser user(){return user;} public String channel(){return channel;}
}
