package com.smartresponse.domain;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="trusted_contacts") public class TrustedContact {
 @Id @GeneratedValue private UUID id; @ManyToOne(optional=false) @JoinColumn(name="owner_id") private AppUser owner;
 private String contactName; private byte[] phoneEncrypted; @Column(name="email_encrypted") private byte[] emailEncrypted; private String relationshipName; private boolean active=true; private Instant createdAt=Instant.now();
 protected TrustedContact(){} public TrustedContact(AppUser owner,String name,byte[] phone,byte[] email,String relation){this.owner=owner;contactName=name;phoneEncrypted=phone;emailEncrypted=email;relationshipName=relation;} public UUID getId(){return id;} public String getContactName(){return contactName;} public byte[] getEmailEncrypted(){return emailEncrypted;} public String getRelationshipName(){return relationshipName;} public boolean isActive(){return active;} public void deactivate(){active=false;}
}
