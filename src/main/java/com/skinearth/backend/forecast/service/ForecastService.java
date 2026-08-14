package com.skinearth.backend.forecast.service;

import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ForecastService {
    private final ForecastRepository forecastRepository;
    private final UserRepository userRepository;

    public ForecastResponseDto createForecast(Long userId, ForecastRequestDto dto) {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        if (forecastRepository.existsByUser_IdAndTargetDate(userId, targetDate)) {
            throw new IllegalArgumentException("이미 존재하는 예보입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Forecast forecast = Forecast.builder()
                .user(user)
                .targetDate(targetDate)
                .inputAc(dto.getInputAc())
                .inputScreenTime(dto.getInputScreenTime())
                .inputSleepHours(dto.getInputSleepHours())
                .inputStress(dto.getInputStress())
                .inputMeal(dto.getInputMeal())
                .build();

        Forecast savedForecast = forecastRepository.save(forecast);
        return ForecastResponseDto.from(savedForecast);
    }

    public ForecastResponseDto getForecast(Long userId) {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        Forecast forecast = forecastRepository.findByUser_IdAndTargetDate(userId, targetDate)
                .orElseThrow(() -> new IllegalArgumentException("해당 예보를 찾을 수 없습니다."));
        return ForecastResponseDto.from(forecast);
    }
}