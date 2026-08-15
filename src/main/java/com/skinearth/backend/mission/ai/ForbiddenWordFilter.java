package com.skinearth.backend.mission.ai;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ForbiddenWordFilter {

    private static final List<String> FORBIDDEN_WORDS = List.of(
            "치료", "낫는다", "완치", "치유", "처방", "진단", "질환", "질병"
    );

    public boolean containsForbiddenWord(String text) {
        if (text == null) return false;
        return FORBIDDEN_WORDS.stream().anyMatch(text::contains);
    }
}