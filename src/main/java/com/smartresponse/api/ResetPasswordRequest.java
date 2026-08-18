package com.smartresponse.api;
import jakarta.validation.constraints.*;
public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min=12,max=100) String newPassword) { }
