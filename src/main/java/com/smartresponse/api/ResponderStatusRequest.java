package com.smartresponse.api;
import jakarta.validation.constraints.*;
public record ResponderStatusRequest(@Pattern(regexp="AVAILABLE|BUSY|OFFLINE") String availabilityStatus,@DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,@DecimalMin("-180.0") @DecimalMax("180.0") Double longitude){}
