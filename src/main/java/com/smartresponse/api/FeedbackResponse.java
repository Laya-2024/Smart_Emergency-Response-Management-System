package com.smartresponse.api;
import java.time.Instant;
import java.util.UUID;
public record FeedbackResponse(UUID id, UUID emergencyId, String emergencyType, int rating, String comments, Instant createdAt, String reporterName) { }
