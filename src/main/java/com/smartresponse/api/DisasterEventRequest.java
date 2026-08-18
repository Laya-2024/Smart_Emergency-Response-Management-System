package com.smartresponse.api;
import jakarta.validation.constraints.*;
public record DisasterEventRequest(@NotBlank @Size(max=32) String eventType,@NotBlank @Size(max=180) String title,@Pattern(regexp="LOW|MEDIUM|HIGH|CRITICAL") String severity,@Size(max=500) String affectedArea) { }
