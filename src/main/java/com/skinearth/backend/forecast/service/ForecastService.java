package com.skinearth.backend.forecast.service;

import com.skinearth.backend.forecast.dto.ForecastRequestDto;
import com.skinearth.backend.forecast.dto.ForecastResponseDto;
import com.skinearth.backend.forecast.entity.Forecast;
import com.skinearth.backend.forecast.repository.ForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ForecastService {
    private final ForecastRepository forecastRepository;

    public ForecastResponseDto createForecast(ForecastRequestDto dto) {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        if(forecastRepository.existsByUserIdAndTargetDate(dto.getUserId(), targetDate)) {
            throw new IllegalArgumentException("이미 존재하는 예보입니다.");
        }
        Forecast forecast = Forecast.builder()
                .userId(dto.getUserId())
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
        Forecast forecast = forecastRepository.findByUserIdAndTargetDate(userId, targetDate)
                .orElseThrow(() -> new IllegalArgumentException("해당 예보를 찾을 수 없습니다."));
        return ForecastResponseDto.from(forecast);
    }
}
