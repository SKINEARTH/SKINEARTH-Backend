package com.skinearth.backend.dailyrecord.repository;

import com.skinearth.backend.dailyrecord.entity.DailyRecord;
import com.skinearth.backend.dailyrecord.entity.SymptomTag;
import com.skinearth.backend.user.entity.User;
import com.skinearth.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class DailyRecordRepositoryTest {

    @Autowired
    private DailyRecordRepository dailyRecordRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsRecordByUserAndDate() {
        User user = userRepository.save(user("one@example.com"));
        LocalDate date = LocalDate.of(2026, 8, 14);
        DailyRecord saved = dailyRecordRepository.saveAndFlush(record(user, date));

        assertThat(dailyRecordRepository.findByUserIdAndRecordDate(user.getId(), date))
                .hasValueSatisfying(found -> {
                    assertThat(found.getId()).isEqualTo(saved.getId());
                    assertThat(found.getSymptoms()).containsExactlyInAnyOrder(
                            SymptomTag.DRYNESS,
                            SymptomTag.REDNESS
                    );
                });
    }

    @Test
    void preventsDuplicateRecordForSameUserAndDate() {
        User user = userRepository.save(user("two@example.com"));
        LocalDate date = LocalDate.of(2026, 8, 14);
        dailyRecordRepository.saveAndFlush(record(user, date));

        assertThatThrownBy(() -> dailyRecordRepository.saveAndFlush(record(user, date)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findsRecordDatesInDescendingOrderUpToGivenDate() {
        User user = userRepository.save(user("dates@example.com"));
        LocalDate today = LocalDate.of(2026, 8, 14);
        dailyRecordRepository.save(record(user, today.minusDays(2)));
        dailyRecordRepository.save(record(user, today));
        dailyRecordRepository.saveAndFlush(record(user, today.minusDays(1)));

        assertThat(dailyRecordRepository.findRecordDatesUpTo(user.getId(), today))
                .containsExactly(today, today.minusDays(1), today.minusDays(2));
    }

    @Test
    void countsOnlyRecordsOwnedByUser() {
        User firstUser = userRepository.save(user("count-one@example.com"));
        User secondUser = userRepository.save(user("count-two@example.com"));
        LocalDate today = LocalDate.of(2026, 8, 14);
        dailyRecordRepository.save(record(firstUser, today));
        dailyRecordRepository.save(record(firstUser, today.minusDays(1)));
        dailyRecordRepository.saveAndFlush(record(secondUser, today));

        assertThat(dailyRecordRepository.countByUserId(firstUser.getId())).isEqualTo(2);
        assertThat(dailyRecordRepository.countByUserId(secondUser.getId())).isOne();
    }

    private User user(String email) {
        return User.builder()
                .email(email)
                .passwordHash("encoded-password")
                .serviceTermsAgreed(true)
                .sensitiveDataAgreed(true)
                .researchDataAgreed(false)
                .build();
    }

    private DailyRecord record(User user, LocalDate date) {
        return DailyRecord.builder()
                .user(user)
                .recordDate(date)
                .acLevel(3)
                .screenTime(4)
                .sleepHours(7)
                .stressLevel(2)
                .mealRegularity(3)
                .skinCondition(4)
                .symptoms(List.of(SymptomTag.DRYNESS, SymptomTag.REDNESS))
                .build();
    }
}
