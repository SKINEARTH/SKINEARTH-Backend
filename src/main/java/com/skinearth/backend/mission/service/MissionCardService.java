package com.skinearth.backend.mission.service;

import com.skinearth.backend.mission.dto.MissionCardResponse;
import com.skinearth.backend.mission.entity.MissionCard;
import com.skinearth.backend.mission.repository.MissionCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MissionCardService {

    private final MissionCardRepository missionCardRepository;

    @Transactional(readOnly = true)
    public MissionCardResponse getTodayCard(Long userId) {
        MissionCard card = missionCardRepository
                .findByUser_IdAndIssuedDate(userId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("오늘 발행된 미션 카드가 없습니다."));
        return MissionCardResponse.from(card);
    }
}