package com.smartresponse.api;
import jakarta.validation.constraints.NotBlank;
public record ChangePasswordRequest(@NotBlank String currentPassword,@NotBlank String newPassword) {}
