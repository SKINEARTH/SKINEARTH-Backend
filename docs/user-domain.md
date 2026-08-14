# User 도메인 협업 가이드

## 구현 범위

이 문서는 기능명세 1.2(약관 동의), 1.3(개인화 설문), 6.5(뱃지 단계)를 기준으로 한 User 도메인의 데이터 계약을 설명합니다.

테이블명은 ERD의 단수형 `user`를 유지합니다. `user`는 일부 DB에서 예약어이므로 JPA 매핑에서 인용 식별자로 지정되어 있습니다.

| Java 필드 | DB 컬럼 | 설명 |
| --- | --- | --- |
| `id` | `id` | 사용자 PK |
| `nickname` | `nickname` | 사용자 닉네임 |
| `userStatus` | `user_status` | `EMPLOYEE`, `STUDENT`, `OTHER` |
| `skinType` | `skin_type` | `OILY`, `DRY`, `COMBINATION`, `NORMAL` |
| `skinConcern` | `skin_concern` | 가장 큰 피부 고민. 선택지 확정 전까지 문자열로 저장 |
| `serviceTermsAgreed` | `service_terms_agreed` | 서비스 이용약관 필수 동의 |
| `sensitiveDataAgreed` | `sensitive_data_agreed` | 건강·생활습관 관련 민감정보 처리 필수 동의 |
| `thirdPartyDataAgreed` | `third_party_data_agreed` | B2B 제3자 활용 선택 동의 |
| `stage` | `stage` | 뱃지 단계. 신규 사용자는 0, 허용 범위는 0~3 |

## 다른 도메인의 연동 지점

- `forecast.user_id`, `daily_record.user_id`, `mission_card.user_id`는 최종적으로 `user.id`를 참조합니다.
- 기존 Forecast 구현은 User 도메인 완성 전까지 `Long userId`를 사용하고 있습니다. 연관관계 전환은 각 도메인 담당자와 합의한 후 별도 이슈에서 진행합니다.
- Badge 조회 로직은 현재 단계 값을 직접 입력받습니다. `User.stage` 연동은 Badge 담당자와 합의한 후 별도 이슈에서 진행합니다.

## 인증 관련

- 이메일 회원가입·로그인은 #18에서 구현합니다.
- 카카오 OAuth 로그인은 #19에서 구현합니다.
- 인증 정보의 테이블 구조는 인증 이슈에서 정의하며, 이 도메인 이슈에는 시연용 우회 로그인을 포함하지 않습니다.

## 미확정 사항

- `skinConcern`의 최종 선택지는 기능명세 Open Issue G가 확정된 후 enum 전환 여부를 결정합니다.
- 사용자 데이터 초기화 정책과 동의 철회 이력 보존 여부는 별도 이슈에서 정의합니다.
