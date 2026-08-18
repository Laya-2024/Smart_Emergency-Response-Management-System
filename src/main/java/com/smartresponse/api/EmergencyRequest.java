package com.smartresponse.api;
import com.smartresponse.domain.EmergencyType;
import jakarta.validation.constraints.*;
public record EmergencyRequest(@NotNull EmergencyType type, @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude, @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude, @Size(max=1000) String description) { }
