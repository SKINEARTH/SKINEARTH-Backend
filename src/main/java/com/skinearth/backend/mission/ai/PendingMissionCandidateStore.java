package com.skinearth.backend.mission.ai;

import com.skinearth.backend.mission.entity.MissionTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingMissionCandidateStore {

    private final Map<Long, PendingCandidate> store = new ConcurrentHashMap<>();

    public void save(Long userId, PendingCandidate candidate) {
        store.put(userId, candidate);
    }

    public Optional<PendingCandidate> get(Long userId) {
        return Optional.ofNullable(store.get(userId));
    }

    public void clear(Long userId) {
        store.remove(userId);
    }

    public record PendingCandidate(MissionTemplate template, String title, String description) {}
}