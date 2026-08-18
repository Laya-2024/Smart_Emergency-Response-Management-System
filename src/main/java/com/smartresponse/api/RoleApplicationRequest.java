package com.smartresponse.api;
import com.smartresponse.domain.Role; import jakarta.validation.constraints.*;
public record RoleApplicationRequest(@NotNull Role requestedRole,@NotBlank @Size(max=150) String organisationName,@NotBlank @Size(max=32) String professionalIdMasked,@NotBlank @Size(max=4000) String detailsJson){}
