package com.smartresponse.api;

import com.smartresponse.domain.EmergencyType;
import com.smartresponse.domain.EmergencyStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(UUID emergencyId, EmergencyType type, double latitude, double longitude,
                            String description, Instant createdAt, EmergencyStatus status,
                            boolean acceptedByCurrentUser) { }
