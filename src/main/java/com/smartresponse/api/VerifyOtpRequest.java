package com.smartresponse.api;
import jakarta.validation.constraints.*;
public record VerifyOtpRequest(@NotBlank @Email String email,@Pattern(regexp="\\d{6}") String code){}
