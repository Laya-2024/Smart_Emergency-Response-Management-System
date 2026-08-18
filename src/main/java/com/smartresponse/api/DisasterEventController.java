package com.smartresponse.api;
import com.smartresponse.domain.DisasterEvent;
import com.smartresponse.repository.DisasterEventRepository;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/disaster-events")
public class DisasterEventController {
 private final DisasterEventRepository events;
 public DisasterEventController(DisasterEventRepository events){this.events=events;}
 @GetMapping public List<DisasterEvent> active(){return events.findByStatusOrderByStartedAtDesc("ACTIVE");}
 @PostMapping public ResponseEntity<DisasterEvent> create(@Valid @RequestBody DisasterEventRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(events.save(new DisasterEvent(r.eventType(),r.title(),r.severity(),r.affectedArea())));}
 @PatchMapping("/{id}/close") public DisasterEvent close(@PathVariable UUID id){DisasterEvent e=events.findById(id).orElseThrow();e.close();return e;}
}
