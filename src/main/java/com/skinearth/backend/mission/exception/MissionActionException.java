package com.skinearth.backend.mission.exception;

import lombok.Getter;

@Getter
public class MissionActionException extends RuntimeException {

    private final String code;

    public MissionActionException(String code, String message) {
        super(message);
        this.code = code;
    }
}
