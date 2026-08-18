package com.smartresponse.config;

import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.time.*; import java.util.concurrent.*;
/** In-memory edge safeguard. Use Redis/Bucket4j in a multi-instance production deployment. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
 private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();
 @Override protected boolean shouldNotFilter(HttpServletRequest r) { return !("POST".equals(r.getMethod()) && "/api/v1/emergencies".equals(r.getServletPath())); }
 @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
   String ip = request.getRemoteAddr(); Window w = windows.compute(ip, (k,v) -> v == null || v.started.plusSeconds(60).isBefore(Instant.now()) ? new Window(Instant.now(),1) : new Window(v.started,v.count+1));
   if (w.count > 5) { response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); response.setContentType("application/json"); response.getWriter().write("{\"detail\":\"Too many SOS attempts. Call 112 if this is urgent.\"}"); return; } chain.doFilter(request,response);
 }
 private record Window(Instant started, int count) { }
}
