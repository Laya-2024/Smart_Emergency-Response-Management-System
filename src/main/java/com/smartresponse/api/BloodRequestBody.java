package com.smartresponse.api;

import jakarta.validation.constraints.*;

public record BloodRequestBody(
    @Pattern(regexp = "(A|B|AB|O)[+-]") String bloodGroup,
    @Min(1) @Max(20) int unitsRequired,
    @NotBlank @Size(max = 180) String hospitalName,
    @Size(max = 1000) String contactNote,
    @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
    @DecimalMin("-180.0") @DecimalMax("180.0") double longitude) { }
