package com.example.parking.global.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterManager {

  // parkingLotId → 연결된 Emitter 목록
  private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(Long parkingLotId) {
    SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 타임아웃

    emitters.computeIfAbsent(parkingLotId, k -> new CopyOnWriteArrayList<>()).add(emitter);

    // 연결 종료 시 제거
    emitter.onCompletion(() -> remove(parkingLotId, emitter));
    emitter.onTimeout(() -> remove(parkingLotId, emitter));
    emitter.onError(e -> remove(parkingLotId, emitter));

    // 최초 연결 시 더미 이벤트 (503 방지)
    try {
      emitter.send(SseEmitter.event().name("connect").data("connected"));
    } catch (IOException e) {
      remove(parkingLotId, emitter);
    }

    return emitter;
  }

  public void notify(Long parkingLotId, Object data) {
    List<SseEmitter> lotEmitters = emitters.getOrDefault(parkingLotId, List.of());

    for (SseEmitter emitter : lotEmitters) {
      try {
        emitter.send(SseEmitter.event().name("spot-update").data(data));
      } catch (IOException e) {
        remove(parkingLotId, emitter);
      }
    }
  }

  private void remove(Long parkingLotId, SseEmitter emitter) {
    List<SseEmitter> lotEmitters = emitters.get(parkingLotId);
    if (lotEmitters != null) {
      lotEmitters.remove(emitter);
    }
  }
}