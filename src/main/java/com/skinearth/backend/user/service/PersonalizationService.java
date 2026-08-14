package com.skinearth.backend.user.service;

import com.skinearth.backend.common.exception.NotFoundException;
import com.skinearth.backend.user.dto.PersonalizationRequest;
import com.skinearth.backend.user.dto.PersonalizationResponse;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizationService {

    private final UserRepository userRepository;

    @Transactional
    public PersonalizationResponse complete(Long userId, PersonalizationRequest request) {
        User user = findUser(userId);
        if (user.isPersonalizationCompleted()) {
            throw new IllegalArgumentException("개인화 설문이 이미 완료되었습니다.");
        }

        user.completePersonalization(
                request.nickname().trim(),
                request.userStatus(),
                request.skinConcerns()
        );
        return PersonalizationResponse.from(user);
    }

    public PersonalizationResponse get(Long userId) {
        User user = findCompletedUser(userId);
        return PersonalizationResponse.from(user);
    }

    @Transactional
    public PersonalizationResponse update(Long userId, PersonalizationRequest request) {
        User user = findCompletedUser(userId);
        user.updatePersonalization(
                request.nickname().trim(),
                request.userStatus(),
                request.skinConcerns()
        );
        return PersonalizationResponse.from(user);
    }

    private User findCompletedUser(Long userId) {
        User user = findUser(userId);
        if (!user.isPersonalizationCompleted()) {
            throw new NotFoundException("완료된 개인화 설문을 찾을 수 없습니다.");
        }
        return user;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
