package com.skinearth.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final int status;
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public ApiResponse(int status, boolean success, String message, T data) {
        this(status, success, null, message, data);
    }

    public ApiResponse(int status, boolean success, String code, String message, T data) {
        this.status = status;
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(status, true, message, data);
    }

    public static <T> ApiResponse<T> success(int status, String message) {
        return new ApiResponse<>(status, true, message, null);
    }

    public static <T> ApiResponse<T> fail(int status, String message) {
        return new ApiResponse<>(status, false, message, null);
    }

    public static <T> ApiResponse<T> fail(int status, String code, String message) {
        return new ApiResponse<>(status, false, code, message, null);
    }
}
