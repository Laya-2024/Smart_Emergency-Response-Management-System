package com.smartresponse.api;
import jakarta.validation.constraints.*;
public record ResetRequest(@NotBlank @Email String email) { }
