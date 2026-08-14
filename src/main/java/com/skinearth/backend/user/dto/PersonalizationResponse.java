package com.skinearth.backend.user.dto;

import com.skinearth.backend.user.entity.SkinConcern;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.entity.UserStatus;

import java.util.Set;

public record PersonalizationResponse(
        String nickname,
        UserStatus userStatus,
        Set<SkinConcern> skinConcerns,
        boolean personalizationCompleted
) {
    public static PersonalizationResponse from(User user) {
        return new PersonalizationResponse(
                user.getNickname(),
                user.getUserStatus(),
                user.getSkinConcerns(),
                user.isPersonalizationCompleted()
        );
    }
}
