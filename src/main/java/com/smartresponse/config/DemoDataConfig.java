package com.smartresponse.config;

import com.smartresponse.domain.AppUser;
import com.smartresponse.domain.Role;
import com.smartresponse.repository.UserRepository;
import com.smartresponse.service.SensitiveDataCrypto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

@Configuration
public class DemoDataConfig {
 @Bean CommandLineRunner demoAdmin(UserRepository users, PasswordEncoder passwords, SensitiveDataCrypto crypto, @Value("${app.demo-mode:true}") boolean demo) {
  return args -> { if(demo && users.findByEmailIgnoreCase("admin@safelink.local").isEmpty()) { AppUser admin=new AppUser("admin@safelink.local","Demo Administrator",passwords.encode("AdminDemo!123"),EnumSet.of(Role.ADMIN),crypto.encrypt("+910000000000"),null);admin.verifyEmail();admin.verifyPhone();users.save(admin); } };
 }
}
