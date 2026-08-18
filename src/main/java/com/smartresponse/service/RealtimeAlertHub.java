package com.smartresponse.service;
import org.springframework.stereotype.Component; import org.springframework.web.servlet.mvc.method.annotation.SseEmitter; import java.io.IOException; import java.util.*; import java.util.concurrent.*;
@Component public class RealtimeAlertHub {
 private final ConcurrentMap<UUID, CopyOnWriteArrayList<SseEmitter>> clients = new ConcurrentHashMap<>();
 public SseEmitter connect(UUID userId) {
  SseEmitter emitter = new SseEmitter(0L);
  clients.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
  emitter.onCompletion(() -> remove(userId, emitter));
  emitter.onTimeout(() -> remove(userId, emitter));
  emitter.onError(ignored -> remove(userId, emitter));
  return emitter;
 }
 public void send(UUID userId, Object event) {
  if (userId == null) return;
  var emitters = clients.getOrDefault(userId, new CopyOnWriteArrayList<>());
  for (SseEmitter emitter : emitters) {
   try {
    emitter.send(SseEmitter.event().name("emergency-alert").data(event));
   } catch (Exception ignored) {
     // A browser tab can be closed while Tomcat is writing an SSE message.
     // Remove that stale connection; alert delivery records remain intact.
     try { emitter.complete(); } catch (Exception ignoredCompletion) { }
     remove(userId, emitter);
   }
  }
 }
 private void remove(UUID id, SseEmitter emitter) {
  if (id == null || emitter == null) return;
  var emitters = clients.get(id);
  if (emitters != null) {
   emitters.remove(emitter);
   if (emitters.isEmpty()) clients.remove(id, emitters);
  }
 }
}
