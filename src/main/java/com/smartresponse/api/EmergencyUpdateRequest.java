package com.smartresponse.api; import jakarta.validation.constraints.*; public record EmergencyUpdateRequest(@NotBlank @Size(max=32) String updateType,@NotBlank @Size(max=2000) String message){}
