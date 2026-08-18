package com.smartresponse.api;
/** Demo-only display codes; never enable this response in production. */
public record RegistrationResult(String emailOtp, String phoneOtp, java.util.UUID applicationId) { }
