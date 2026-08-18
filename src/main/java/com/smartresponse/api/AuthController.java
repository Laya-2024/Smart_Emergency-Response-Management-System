package com.smartresponse.api;
import com.smartresponse.service.AccountService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/auth") public class AuthController {
 private final AccountService accounts; public AuthController(AccountService accounts){this.accounts=accounts;}
 @PostMapping("/register") public ResponseEntity<Map<String,Object>> register(@Valid @RequestBody RegistrationRequest body){RegistrationResult r=accounts.register(body);Map<String,Object> response=new LinkedHashMap<>();response.put("message","Verify email and phone before signing in.");response.put("demoEmailOtp",r.emailOtp());response.put("demoPhoneOtp",r.phoneOtp());response.put("applicationId",r.applicationId());return ResponseEntity.status(HttpStatus.CREATED).body(response);}
 @PostMapping("/verify/email") public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyOtpRequest body){accounts.verify(body.email(),body.code(),"EMAIL");return ResponseEntity.noContent().build();}
 @PostMapping("/verify/phone") public ResponseEntity<Void> verifyPhone(@Valid @RequestBody VerifyOtpRequest body){accounts.verify(body.email(),body.code(),"PHONE");return ResponseEntity.noContent().build();}
 @PostMapping("/password-reset") public ResponseEntity<Void> requestReset(@Valid @RequestBody ResetRequest body){accounts.requestReset(body);return ResponseEntity.accepted().build();}
 @PostMapping("/password-reset/confirm") public ResponseEntity<Map<String,String>> reset(@Valid @RequestBody ResetPasswordRequest body){accounts.resetPassword(body);return ResponseEntity.ok(Map.of("message","Password updated. You can sign in now."));}
}
