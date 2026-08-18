package com.smartresponse.api;
import jakarta.validation.constraints.*;
public record ReviewRequest(boolean approved,@Size(max=1000) String notes){}
