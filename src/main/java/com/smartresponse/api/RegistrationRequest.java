package com.smartresponse.api;
import com.smartresponse.domain.Role; import jakarta.validation.constraints.*; import java.util.Set;
public record RegistrationRequest(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=2,max=100) String fullName, @Pattern(regexp="\\+?[0-9]{10,15}") String phone, @NotBlank @Size(min=8,max=100) String password, @NotEmpty Set<Role> roles, @Size(max=150) String organisationName, @Size(max=32) String professionalIdMasked, @Size(max=4000) String detailsJson) { }
