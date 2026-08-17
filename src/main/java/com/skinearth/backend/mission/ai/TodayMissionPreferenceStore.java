package com.skinearth.backend.mission.ai;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TodayMissionPreferenceStore {

    private final Map<UserDayKey, Preferences> preferencesByDay = new ConcurrentHashMap<>();

    public Set<String> getExcludedCategories(Long userId, LocalDate date) {
        return preferences(userId, date).excludedCategories();
    }

    public Set<String> getSeenActionTypes(Long userId, LocalDate date) {
        return preferences(userId, date).seenActionTypes();
    }

    public void excludeCategory(Long userId, LocalDate date, String category) {
        preferences(userId, date).excludedCategories().add(category);
    }

    public void addSeenActionType(Long userId, LocalDate date, String actionType) {
        preferences(userId, date).seenActionTypes().add(actionType);
    }

    public void clearSeenActionTypes(Long userId, LocalDate date) {
        preferences(userId, date).seenActionTypes().clear();
    }

    public void clearForUser(Long userId) {
        preferencesByDay.keySet().removeIf(key -> key.userId().equals(userId));
    }

    private Preferences preferences(Long userId, LocalDate date) {
        return preferencesByDay.computeIfAbsent(new UserDayKey(userId, date), ignored -> new Preferences());
    }

    private record UserDayKey(Long userId, LocalDate date) {
    }

    private static final class Preferences {
        private final Set<String> excludedCategories = ConcurrentHashMap.newKeySet();
        private final Set<String> seenActionTypes = ConcurrentHashMap.newKeySet();

        private Set<String> excludedCategories() {
            return excludedCategories;
        }

        private Set<String> seenActionTypes() {
            return seenActionTypes;
        }
    }
}
