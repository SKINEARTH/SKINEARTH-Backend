# SKINEARTH Backend API

현재 `dev` 브랜치에 구현 완료된 API를 정리한 문서입니다. 프론트엔드 연동과 Swagger 테스트 시 함께 사용합니다.

## 1. 기본 정보

- Base URL(로컬): `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- 요청·응답 형식: `application/json`
- 날짜: `YYYY-MM-DD`
- 날짜와 시간: ISO-8601 형식

### 공통 성공 응답

```json
{
  "status": 200,
  "success": true,
  "message": "요청에 성공했습니다.",
  "data": {}
}
```

### 공통 실패 응답

```json
{
  "status": 400,
  "success": false,
  "message": "오류 메시지"
}
```

### JWT 인증

회원가입과 로그인을 제외한 API는 로그인 응답에서 받은 JWT가 필요합니다.

```http
Authorization: Bearer {accessToken}
```

로그아웃 API는 별도로 없습니다. 클라이언트가 보관 중인 JWT를 삭제하면 로그아웃됩니다.

## 2. Enum 값

### 사용자 상태 `UserStatus`

| 값 | 의미 |
| --- | --- |
| `EMPLOYEE` | 직장인 |
| `STUDENT` | 학생 |
| `OTHER` | 기타 |

### 피부 고민 `SkinConcern`

| 값 | 의미 |
| --- | --- |
| `DRYNESS` | 건조함 |
| `SENSITIVITY` | 민감성 |
| `TROUBLE` | 트러블 |
| `DULLNESS` | 칙칙함 |
| `PORES` | 모공 |
| `OILINESS` | 기름기 |

### 증상 태그 `SymptomTag`

| 값 | 의미 |
| --- | --- |
| `DRYNESS` | 건조 |
| `REDNESS` | 홍조 |
| `TROUBLE` | 트러블 |
| `OILINESS` | 유분 |
| `SENSITIVITY` | 민감 |
| `NONE` | 없음 |

`NONE`은 다른 증상과 함께 보낼 수 없습니다.

### 미션 상태 `MissionExecutionStatus`

| 값 | 의미 |
| --- | --- |
| `PENDING` | 오늘 발행되었고 아직 미체크 |
| `COMPLETED` | 완료 체크됨 |
| `FAILED` | 발행일이 지났지만 미체크 |

## 3. 인증

### 3.1 회원가입

`POST /api/auth/signup`

인증: 불필요

```json
{
  "email": "test@example.com",
  "password": "test1234!",
  "passwordConfirm": "test1234!",
  "serviceTermsAgreed": true,
  "sensitiveDataAgreed": true,
  "researchDataAgreed": false
}
```

- 비밀번호: 8~72자
- 서비스 이용약관과 민감정보 처리는 필수 동의
- 연구 목적 데이터 활용은 선택 동의

성공: `201 Created`

```json
{
  "status": 201,
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1,
    "email": "test@example.com"
  }
}
```

### 3.2 로그인

`POST /api/auth/login`

인증: 불필요

```json
{
  "email": "test@example.com",
  "password": "test1234!"
}
```

성공: `200 OK`

```json
{
  "status": 200,
  "success": true,
  "message": "로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "personalizationCompleted": false
  }
}
```

## 4. 온보딩·개인화

### 4.1 개인화 설문 저장

`POST /api/users/me/personalization`

성공: `201 Created`

```json
{
  "nickname": "여행자",
  "userStatus": "EMPLOYEE",
  "skinConcerns": ["DRYNESS", "PORES"]
}
```

- 닉네임: 필수, 최대 30자
- 피부 고민: 1개 이상, 중복 불가
- 최초 1회 저장 API이며 이미 완료한 사용자는 수정 API를 사용합니다.

### 4.2 개인화 정보 조회

`GET /api/users/me/personalization`

```json
{
  "status": 200,
  "success": true,
  "message": "개인화 정보를 조회했습니다.",
  "data": {
    "nickname": "여행자",
    "userStatus": "EMPLOYEE",
    "skinConcerns": ["DRYNESS", "PORES"],
    "personalizationCompleted": true
  }
}
```

### 4.3 개인화 정보 수정

`PUT /api/users/me/personalization`

요청 형식은 개인화 설문 저장과 같습니다.

### 4.4 온보딩·첫 기록 진행 상태

`GET /api/users/me/onboarding-status`

```json
{
  "status": 200,
  "success": true,
  "message": "온보딩 상태를 조회했습니다.",
  "data": {
    "nickname": "여행자",
    "personalizationCompleted": true,
    "validRecordCount": 3,
    "targetRecordCount": 10,
    "remainingRecordCount": 7,
    "firstRecordCompleted": true,
    "forecastReady": false
  }
}
```

## 5. 일일 기록

### 공통 요청 형식

```json
{
  "acLevel": 3,
  "screenTime": 4,
  "sleepHours": 7,
  "stressLevel": 2,
  "mealRegularity": 3,
  "skinCondition": 4,
  "symptoms": ["DRYNESS", "REDNESS"]
}
```

| 필드 | 범위 | 필수 여부 |
| --- | --- | --- |
| `acLevel` | 1~5 | 환경요인 중 하나 이상 |
| `screenTime` | 1~5 | 환경요인 중 하나 이상 |
| `sleepHours` | 0~24 | 환경요인 중 하나 이상 |
| `stressLevel` | 1~5 | 환경요인 중 하나 이상 |
| `mealRegularity` | 1~5 | 환경요인 중 하나 이상 |
| `skinCondition` | 1~5 | 필수 |
| `symptoms` | `SymptomTag[]` | 선택 |

### 5.1 오늘 기록 저장

`POST /api/daily-records/today`

- 하루에 한 건만 저장할 수 있습니다.
- 성공: `201 Created`

### 5.2 오늘 기록 조회

`GET /api/daily-records/today`

### 5.3 오늘 기록 수정

`PUT /api/daily-records/today`

- 오늘 기록만 수정할 수 있습니다.
- 수정은 스트릭에 영향을 주지 않습니다.

### 일일 기록 응답 예시

```json
{
  "status": 200,
  "success": true,
  "message": "오늘의 기록을 조회했습니다.",
  "data": {
    "id": 1,
    "recordDate": "2026-08-14",
    "acLevel": 3,
    "screenTime": 4,
    "sleepHours": 7,
    "stressLevel": 2,
    "mealRegularity": 3,
    "skinCondition": 4,
    "symptoms": ["DRYNESS", "REDNESS"],
    "currentStreak": 3,
    "validRecordCount": 3,
    "targetRecordCount": 10,
    "forecastReady": false,
    "forecastTransitionReached": false
  }
}
```

스트릭은 전날 기록부터 연속된 일수를 계산합니다. 전날 기록이 없다면 `0`입니다.

## 6. 내일의 궤도 예보 — 콜드스타트

개인화 설문을 완료했으며 유효 기록이 10건 미만인 사용자가 이용합니다.

### 6.1 콜드스타트 예보 생성

`POST /api/forecasts`

```json
{
  "inputAc": 3,
  "inputScreenTime": 4,
  "inputSleepHours": 6,
  "inputStress": 2,
  "inputMeal": 4
}
```

- 다섯 입력값은 모두 필수입니다.
- 냉난방·스크린타임·스트레스·식사 규칙성은 1~5, 수면은 0~24시간입니다.
- 사용자 상태와 피부 고민으로 정한 상위 원인 2개의 가중평균을 반올림하여 위험도를 계산합니다.
- 같은 날짜의 예보는 한 번만 생성할 수 있습니다.

성공: `201 Created`. 응답의 `source`는 `COLD_START`이며 `primaryFactors`에 원인, 우선순위 점수와 순위가 포함됩니다.

### 6.2 내일 예보 조회

`GET /api/forecasts`

JWT 사용자에게 저장된 내일 예보를 조회합니다.

## 7. 궤도 히스토리

### 주간·월간 기록 조회

`GET /api/history?period={period}&date={date}`

| Query | 필수 | 설명 |
| --- | --- | --- |
| `period` | 필수 | `WEEKLY` 또는 `MONTHLY` |
| `date` | 선택 | 기준 날짜. 생략 시 오늘 |

- 주간: 기준 날짜가 속한 월요일~일요일
- 월간: 기준 날짜가 속한 달의 1일~말일
- 기록이 없는 날짜도 `skinCondition: null`로 포함됩니다.

```json
{
  "status": 200,
  "success": true,
  "message": "궤도 히스토리를 조회했습니다.",
  "data": {
    "period": "WEEKLY",
    "startDate": "2026-08-10",
    "endDate": "2026-08-16",
    "recordCount": 2,
    "averageSkinCondition": 3.5,
    "points": [
      {"date": "2026-08-10", "skinCondition": 3},
      {"date": "2026-08-11", "skinCondition": null},
      {"date": "2026-08-12", "skinCondition": 4}
    ]
  }
}
```

기록이 한 건도 없으면 `averageSkinCondition`은 `null`입니다.

## 8. 마이페이지

### 7.1 마이페이지 정보 조회

`GET /api/users/me`

```json
{
  "status": 200,
  "success": true,
  "message": "마이페이지 정보를 조회했습니다.",
  "data": {
    "email": "test@example.com",
    "joinedDate": "2026-08-14",
    "nickname": "여행자",
    "userStatus": "EMPLOYEE",
    "skinConcerns": ["DRYNESS", "PORES"],
    "personalizationCompleted": true,
    "stage": 1,
    "badgeName": "관측자",
    "currentStreak": 3
  }
}
```

### 7.2 데이터 전체 초기화

`POST /api/users/me/data-reset`

```json
{
  "confirmed": true
}
```

초기화되는 데이터:

- 일일 기록과 증상
- 예보
- 미션 카드
- 개인화 설문
- 뱃지 단계와 스트릭

유지되는 데이터:

- 이메일과 비밀번호
- 가입일
- 약관 동의 정보

## 9. 미션 이행

미션 생성 기능에서 오늘의 미션 카드가 발행된 이후 사용할 수 있습니다.

### 8.1 오늘 미션 조회

`GET /api/missions/today`

```json
{
  "status": 200,
  "success": true,
  "message": "오늘의 미션 카드를 조회했습니다.",
  "data": {
    "id": 10,
    "category": "긴장 완화",
    "title": "짧은 산책하기",
    "description": "잠시 걸으며 긴장을 풀어보세요.",
    "issuedDate": "2026-08-14",
    "isCompleted": false,
    "isReplaced": false,
    "completedAt": null
  }
}
```

### 8.2 미션 완료

`POST /api/missions/{missionCardId}/complete`

- 오늘 발행된 본인 미션만 완료할 수 있습니다.
- 이미 완료한 미션은 다시 완료할 수 없습니다.
- 별도의 Request Body는 없습니다.

```json
{
  "status": 200,
  "success": true,
  "message": "미션 수행을 완료했습니다.",
  "data": {
    "missionCardId": 10,
    "category": "긴장 완화",
    "issuedDate": "2026-08-14",
    "completed": true,
    "replaced": false,
    "completedAt": "2026-08-14T18:30:00",
    "status": "COMPLETED"
  }
}
```

### 8.3 주간 미션 이행률 조회

`GET /api/missions/history/weekly?date={date}`

- `date`는 선택이며 생략하면 오늘을 기준으로 합니다.
- 해당 주 월요일~일요일의 카드가 반환됩니다.
- 발행일이 지난 미체크 카드는 조회 시 `FAILED`로 표시됩니다.

```json
{
  "status": 200,
  "success": true,
  "message": "주간 미션 이행 기록을 조회했습니다.",
  "data": {
    "startDate": "2026-08-10",
    "endDate": "2026-08-16",
    "issuedCount": 3,
    "completedCount": 1,
    "completionRatePercent": 33.3,
    "cards": [
      {
        "missionCardId": 10,
        "category": "긴장 완화",
        "issuedDate": "2026-08-14",
        "completed": false,
        "replaced": false,
        "completedAt": null,
        "status": "PENDING"
      }
    ]
  }
}
```

## 10. 로컬 실행 환경 변수

MySQL 비밀번호가 없는 경우 IntelliJ 실행 구성에 다음 환경 변수를 설정합니다.

```ini
DB_USERNAME=root;DB_PASSWORD=;JWT_SECRET=skinearth-local-development-secret-key-2026
```

`JWT_SECRET`은 32바이트 이상이어야 합니다. 위 값은 로컬 개발 예시이며 배포 환경에서는 별도의 안전한 값을 사용합니다.

## 11. 권장 연동 테스트 순서

1. 회원가입
2. 로그인 후 JWT 저장
3. 개인화 설문 저장
4. 온보딩 상태 조회
5. 오늘 기록 저장·조회·수정
6. 주간·월간 히스토리 조회
7. 마이페이지 조회
8. 미션이 발행된 경우 오늘 미션 조회·완료·주간 이행률 조회
9. 테스트 종료 후 필요하면 데이터 초기화

## 12. 아직 완료되지 않은 연동

다음 항목은 구현 또는 기획 기준 확정 후 이 문서에 추가합니다.

- 4.6 위험도·등급·원인·AI 코멘트를 포함한 예보 결과 저장
- 2.1~2.5 홈 통합 API
- 5.4 미션 교체 및 퀵리플라이 연동
