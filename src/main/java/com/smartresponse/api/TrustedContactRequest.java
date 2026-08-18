package com.smartresponse.api;
import jakarta.validation.constraints.*;
public record TrustedContactRequest(@NotBlank @Size(max=100) String contactName,@Pattern(regexp="\\+?[0-9]{10,15}") String phone,@NotBlank @Email @Size(max=254) String email,@Size(max=60) String relationshipName){}
