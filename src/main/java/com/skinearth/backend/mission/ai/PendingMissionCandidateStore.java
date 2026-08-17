package com.skinearth.backend.mission.ai;

import com.skinearth.backend.mission.entity.MissionTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingMissionCandidateStore {

    private final Map<UserDayKey, PendingCandidate> store = new ConcurrentHashMap<>();

    public void save(Long userId, LocalDate date, PendingCandidate candidate) {
        store.put(new UserDayKey(userId, date), candidate);
    }

    public Optional<PendingCandidate> get(Long userId, LocalDate date) {
        return Optional.ofNullable(store.get(new UserDayKey(userId, date)));
    }

    public void clear(Long userId, LocalDate date) {
        store.remove(new UserDayKey(userId, date));
    }

    public void clearForUser(Long userId) {
        store.keySet().removeIf(key -> key.userId().equals(userId));
    }

    public record PendingCandidate(MissionTemplate template, String title, String description) {}

    private record UserDayKey(Long userId, LocalDate date) {}
}
