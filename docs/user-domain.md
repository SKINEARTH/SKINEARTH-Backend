# User 도메인 협업 가이드

## 구현 범위

이 문서는 기능명세 1.2(약관 동의), 1.3(개인화 설문), 6.5(뱃지 단계)를 기준으로 한 User 도메인의 데이터 계약을 설명합니다.

테이블명은 ERD의 단수형 `user`를 유지합니다. `user`는 일부 DB에서 예약어이므로 JPA 매핑에서 인용 식별자로 지정되어 있습니다.

| Java 필드 | DB 컬럼 | 설명 |
| --- | --- | --- |
| `id` | `id` | 사용자 PK |
| `email` | `email` | 로그인 이메일, 대소문자 무시 유일값 |
| `passwordHash` | `password_hash` | BCrypt 단방향 암호화 비밀번호 |
| `nickname` | `nickname` | 사용자 닉네임. 가입 직후에는 null 가능 |
| `userStatus` | `user_status` | `EMPLOYEE`, `STUDENT`, `OTHER`. 가입 직후에는 null 가능 |
| `skinConcerns` | `user_skin_concern.skin_concern` | 피부 고민 다중 선택. 개인화 설문 완료 전에는 빈 컬렉션 |
| `personalizationCompleted` | `personalization_completed` | 개인화 설문 완료 여부 |
| `serviceTermsAgreed` | `service_terms_agreed` | 서비스 이용약관 필수 동의 |
| `sensitiveDataAgreed` | `sensitive_data_agreed` | 건강·생활습관 관련 민감정보 처리 필수 동의 |
| `researchDataAgreed` | `research_data_agreed` | 연구목적 데이터 공유 선택 동의 |
| `stage` | `stage` | 뱃지 단계. 신규 사용자는 1단계 관측자, 허용 범위는 1~3 |

## 다른 도메인의 연동 지점

- `forecast.user_id`, `daily_record.user_id`, `mission_card.user_id`는 최종적으로 `user.id`를 참조합니다.
- 기존 Forecast 구현은 User 도메인 완성 전까지 `Long userId`를 사용하고 있습니다. 연관관계 전환은 각 도메인 담당자와 합의한 후 별도 이슈에서 진행합니다.
- Badge 조회 로직은 현재 단계 값을 직접 입력받습니다. `User.stage` 연동은 Badge 담당자와 합의한 후 별도 이슈에서 진행합니다.

## 인증 관련

- 이메일 회원가입·로그인은 #18에서 구현합니다. 상세 계약은 [이메일 인증 협업 가이드](auth.md)를 참고합니다.
- 카카오 로그인은 최신 화면 범위에 포함되지 않아 #19를 진행하지 않기로 결정했습니다.
- 인증 정보의 테이블 구조는 #18에서 정의하며, 시연용 우회 로그인을 포함하지 않습니다.

로그인 응답의 `personalizationCompleted` 값으로 프론트엔드가 개인화 설문 화면 진입 여부를 판단합니다.

## 개인화 설문 선택지

- 현재 상태: `EMPLOYEE`, `STUDENT`, `OTHER`
- 피부 고민: `DRYNESS`, `SENSITIVITY`, `TROUBLE`, `DULLNESS`, `PORES`, `OILINESS`
- 피부 타입은 최신 화면에서 제외되어 저장하지 않습니다.
- 피부 고민은 1개 이상 선택하며 중복 선택할 수 없습니다.

## 미확정 사항

- 사용자 데이터 초기화 정책과 동의 철회 이력 보존 여부는 별도 이슈에서 정의합니다.
