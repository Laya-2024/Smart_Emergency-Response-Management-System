package com.smartresponse.api;

import com.smartresponse.domain.EmergencyStatus;
import com.smartresponse.repository.EmergencyRepository;
import com.smartresponse.repository.UserRepository;
import com.smartresponse.service.EmergencyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emergencies")
public class EmergencyController {
 private final EmergencyService service;
 private final UserRepository users;
 private final EmergencyRepository emergencies;

 public EmergencyController(EmergencyService service, UserRepository users, EmergencyRepository emergencies) {
  this.service = service;
  this.users = users;
  this.emergencies = emergencies;
 }

 @PostMapping
 public ResponseEntity<EmergencyResponse> create(
   @RequestHeader("Idempotency-Key") String idempotencyKey,
   @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
   @Valid @RequestBody EmergencyRequest request,
   Authentication authentication) {
  UUID reporterId = reporterId(authentication, deviceId);
  return ResponseEntity.status(HttpStatus.CREATED).body(service.create(reporterId, idempotencyKey, request));
 }

 @GetMapping("/mine")
 public List<EmergencyResponse> mine(Authentication authentication) {
  UUID rid = users.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
  return emergencies.findByReporterIdOrderByCreatedAtDesc(rid).stream()
    .map(e -> new EmergencyResponse(e.getId(), e.getType(), e.getStatus(), e.getLatitude(), e.getLongitude(), e.getCreatedAt()))
    .toList();
 }

 @GetMapping
 public Page<EmergencyResponse> list(
   @RequestParam(required = false) EmergencyStatus status,
   @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
  return service.list(status, pageable);
 }

 @PatchMapping("/{id}/acknowledge")
 public EmergencyResponse acknowledge(@PathVariable UUID id) { return service.acknowledge(id); }

 @PatchMapping("/{id}/resolve")
 public EmergencyResponse resolve(@PathVariable UUID id, Authentication authentication) { return service.resolve(id, reporterId(authentication, null)); }

 @PatchMapping("/{id}/cancel")
 public EmergencyResponse cancel(@PathVariable UUID id, Authentication authentication) {
  return service.cancel(id, reporterId(authentication, null));
 }

 private UUID reporterId(Authentication authentication, String deviceId) {
  if (authentication != null) return users.findByEmailIgnoreCase(authentication.getName()).orElseThrow().getId();
  String anonymousIdentity = "anonymous:" + (deviceId == null ? "unidentified" : deviceId);
  return UUID.nameUUIDFromBytes(anonymousIdentity.getBytes(StandardCharsets.UTF_8));
 }
}
