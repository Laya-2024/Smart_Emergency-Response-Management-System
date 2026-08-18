package com.smartresponse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
 @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
  return http.csrf(csrf -> csrf.disable())
   .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'; connect-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'")))
   .authorizeHttpRequests(a -> a
    .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/verify.html", "/forgot-password.html", "/reset-password.html", "/manifest.webmanifest", "/css/**", "/js/**", "/actuator/health", "/api/v1/auth/**").permitAll()
    .requestMatchers("/portal.html", "/dashboard.html", "/feedback.html", "/apply-role.html", "/profile.html", "/operations.html").authenticated()
    .requestMatchers("/admin.html").hasRole("ADMIN")
    .requestMatchers("/api/v1/feedback").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/api/v1/emergencies/*/accept").authenticated()
    .requestMatchers(HttpMethod.POST, "/api/v1/shelters").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/api/v1/disaster-events").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PATCH, "/api/v1/disaster-events/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PATCH, "/api/v1/shelters/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/api/v1/emergencies/*/timeline").hasAnyRole("POLICE","DOCTOR","FIRE","PARAMEDIC","VOLUNTEER","DISPATCHER","ADMIN")
    .requestMatchers("/api/v1/role-applications/pending", "/api/v1/role-applications/*/review").hasRole("ADMIN")
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PATCH, "/api/v1/emergencies/*/resolve").authenticated()
    .requestMatchers("/api/v1/emergencies/*/acknowledge").hasAnyRole("DISPATCHER", "ADMIN")
    .requestMatchers("/api/**").authenticated().anyRequest().denyAll())
   .formLogin(f -> f.loginPage("/login.html").loginProcessingUrl("/login").defaultSuccessUrl("/portal.html",true).failureUrl("/login.html?error").permitAll())
   .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login.html?logout=true").permitAll())
   // Form login is used by this web app. Disable HTTP Basic so browsers never
   // show a native username/password popup for an API request.
   .httpBasic(httpBasic -> httpBasic.disable()).build();
 }
 @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
