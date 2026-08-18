package com.smartresponse.api;
import com.smartresponse.domain.*;
import java.time.Instant; import java.util.UUID;
public record EmergencyResponse(UUID id, EmergencyType type, EmergencyStatus status, double latitude, double longitude, Instant createdAt) { }
