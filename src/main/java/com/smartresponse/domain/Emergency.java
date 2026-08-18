package com.smartresponse.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "emergencies", indexes = { @Index(name = "idx_emergency_status_created", columnList = "status,created_at"), @Index(name = "idx_emergency_type", columnList = "type") })
public class Emergency {
    @Id @GeneratedValue private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private EmergencyType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private EmergencyStatus status = EmergencyStatus.OPEN;
    @Column(name = "reporter_id", nullable = false) private UUID reporterId;
    @Column(name = "idempotency_key", nullable = false, length = 80, unique = true) private String idempotencyKey;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(length = 1000) private String description;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at") private Instant updatedAt = Instant.now();
    @Version private long version;
    protected Emergency() { }
    public Emergency(EmergencyType type, UUID reporterId, String idempotencyKey, double latitude, double longitude, String description) { this.type=type; this.reporterId=reporterId; this.idempotencyKey=idempotencyKey; this.latitude=latitude; this.longitude=longitude; this.description=description; }
    @PreUpdate void updateTimestamp() { updatedAt = Instant.now(); }
    public UUID getId() { return id; } public UUID getReporterId(){return reporterId;} public EmergencyStatus getStatus() { return status; } public EmergencyType getType() { return type; } public double getLatitude() { return latitude; } public double getLongitude() { return longitude; } public String getDescription() { return description; } public Instant getCreatedAt() { return createdAt; }
    public void acknowledge() { if (status != EmergencyStatus.OPEN) throw new IllegalStateException("Only new cases can be acknowledged"); status = EmergencyStatus.ACKNOWLEDGED; }
    public void resolve() { if (status != EmergencyStatus.ACKNOWLEDGED && status != EmergencyStatus.IN_PROGRESS) throw new IllegalStateException("Only an acknowledged emergency can be resolved"); status = EmergencyStatus.RESOLVED; }
    public void cancel() { if (status != EmergencyStatus.OPEN) throw new IllegalStateException("Only an unacknowledged emergency can be cancelled"); status = EmergencyStatus.CANCELLED; }
}
