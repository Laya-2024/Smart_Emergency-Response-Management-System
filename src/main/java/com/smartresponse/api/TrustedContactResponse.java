package com.smartresponse.api;
import java.util.UUID;
public record TrustedContactResponse(UUID id,String contactName,String relationshipName,boolean active){}
