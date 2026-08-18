package com.smartresponse.api;
import com.smartresponse.service.TrustedContactService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/trusted-contacts") public class TrustedContactController {
 private final TrustedContactService service; public TrustedContactController(TrustedContactService service){this.service=service;}
 @GetMapping public List<TrustedContactResponse> list(Authentication a){return service.list(a.getName());}
 @PostMapping public ResponseEntity<TrustedContactResponse> add(Authentication a,@Valid @RequestBody TrustedContactRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.add(a.getName(),r));}
 @DeleteMapping("/{id}") public ResponseEntity<Void> remove(Authentication a,@PathVariable UUID id){service.remove(a.getName(),id);return ResponseEntity.noContent().build();}
}
