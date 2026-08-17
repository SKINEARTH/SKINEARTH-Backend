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

## 6. 내일의 궤도 예보

개인화 설문을 완료한 사용자가 이용합니다. 유효 기록이 10건 미만이면 콜드스타트 프리셋을, 10건 이상이면 개인 기록 기반 통계 결과를 사용합니다.

### 6.1 예보 생성

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
- 콜드스타트에서는 사용자 상태·피부 고민 가중치와 다섯 환경 요인을 모두 반영해 위험도를 계산합니다.
- 데이터 기반 예보에서는 최근 기록에서 계산한 요인별 상관관계를 가중치로 사용해 다섯 환경 요인을 모두 반영합니다.
- `primaryFactors`에는 현재 위험도가 높은 주요 원인 2개만 표시합니다.
- 같은 날짜의 예보는 한 번만 생성할 수 있습니다.

성공: `201 Created`. 응답의 `source`는 `COLD_START` 또는 `DATA_BASED`이며 `primaryFactors`에 주요 원인과 등급이 포함됩니다.

### 6.2 내일 예보 수정

`PUT /api/forecasts`

생성된 내일 예보의 다섯 환경 입력값을 수정하고 위험도·주요 원인·AI 코멘트를 재계산합니다.

- 요청 본문은 `POST /api/forecasts`와 동일합니다.
- 저장된 내일 예보가 없으면 `404 Not Found`를 반환합니다.
- 생성 여부는 먼저 `GET /api/forecasts`로 확인합니다. `404`면 `POST`, 예보가 있으면 기존 입력값을 채워 `PUT`을 호출합니다.

### 6.3 내일 예보 조회

`GET /api/forecasts`

JWT 사용자에게 저장된 내일 예보를 조회합니다.

예보 응답에는 수정 화면에 필요한 기존 입력값과 계산 결과가 함께 포함됩니다.

```json
{
  "id": 1,
  "targetDate": "2026-08-18",
  "inputAc": 3,
  "inputScreenTime": 4,
  "inputSleepHours": 6,
  "inputStress": 2,
  "inputMeal": 4,
  "riskScore": 55,
  "riskLevel": "보통",
  "source": "COLD_START",
  "validRecordCount": 7,
  "aiComment": "내일의 피부 컨디션을 위한 안내 문구입니다.",
  "isCommentFallback": false,
  "createdAt": "2026-08-17T12:00:00",
  "primaryFactors": [
    {
      "name": "냉난방 노출",
      "level": "보통",
      "priorityScore": 6,
      "rank": 1
    }
  ]
}
```

주요 원인 이름은 `냉난방 노출`, `화면 노출`, `수면 시간`, `스트레스`, `식사 규칙성` 중 하나입니다.

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

### 주요 원인 변화 타임라인

`GET /api/history/cause-timeline/weekly`

`GET /api/history/cause-timeline/monthly`

- 주간은 이번 주 월요일~일요일, 월간은 이번 달 1일~말일의 일일 기록을 조회합니다.
- 각 기록의 다섯 원인을 위험도로 환산해 가장 높은 원인을 그날의 주요 원인으로 선택합니다.
- 동일한 원인과 등급이 날짜상 연속될 때 하나의 구간으로 묶어 최신순으로 반환합니다.
- 기록이 없으면 정상 응답으로 빈 배열을 반환합니다.

```json
{
  "status": 200,
  "success": true,
  "message": "주간 주요 원인 변화를 조회했습니다.",
  "data": [
    {
      "startDate": "2026-08-14",
      "endDate": "2026-08-15",
      "factorName": "에어컨 노출",
      "level": "위험"
    },
    {
      "startDate": "2026-08-12",
      "endDate": "2026-08-13",
      "factorName": "스트레스",
      "level": "주의"
    }
  ]
}
```

## 8. 마이페이지

### 8.1 마이페이지 정보 조회

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

### 8.2 데이터 전체 초기화

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
- 당일 대체 미션 후보와 카테고리 제외 상태

유지되는 데이터:

- 이메일과 비밀번호
- 가입일
- 약관 동의 정보

### 8.3 PP 여행 단계 조회

`GET /api/users/stage`

현재 PP 단계와 다음 단계까지의 진행 조건을 조회합니다.

```json
{
  "status": 200,
  "success": true,
  "message": "단계 조회 성공",
  "data": {
    "stage": 1,
    "name": "관측자",
    "description": "가장 기본적인 상태의 인실리씨 PP입니다.",
    "conditionDescription": "레코드를 10건 이상 기록하세요.",
    "progressList": [
      {"label": "궤도를 기록하기", "current": 3, "target": 10}
    ]
  }
}
```

## 9. 미션 이행

개인화 설문 완료 후 오늘의 미션 카드가 발행된 이후 사용할 수 있습니다. 개인화 전 사용자가 오늘 카드를 새로 생성하려 하면 `400 Bad Request`를 반환합니다.

### 9.1 오늘 미션 조회

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
    "estimatedMinutes": 3,
    "issuedDate": "2026-08-14",
    "isCompleted": false,
    "isReplaced": false,
    "completedAt": null,
    "streak": 2
  }
}
```

### 9.2 미션 완료

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

### 9.3 주간 미션 완료율 조회

`GET /api/missions/history/weekly`

- 이번 주 월요일부터 일요일까지의 미션 완료 목표와 발행·완료 수를 조회합니다.
- `achievementLevel`은 목표 진행률 기준으로 `HIGH_PROGRESS`(70% 이상), `MID_PROGRESS`(40% 이상), `LOW_PROGRESS`(40% 미만)를 반환합니다.

```json
{
  "status": 200,
  "success": true,
  "message": "주간 미션 이행 기록을 조회했습니다.",
  "data": {
    "startDate": "2026-08-10",
    "endDate": "2026-08-16",
    "targetCount": 7,
    "issuedCount": 3,
    "completedCount": 1,
    "completionRatePercent": 14.3,
    "achievementLevel": "LOW_PROGRESS"
  }
}
```

### 9.4 월간 미션 완료율 조회

`GET /api/missions/history/monthly`

이번 달 1일~말일에 발행된 미션의 완료율을 집계합니다.

```json
{
  "status": 200,
  "success": true,
  "message": "월간 미션 완료율을 조회했습니다.",
  "data": {
    "startDate": "2026-08-01",
    "endDate": "2026-08-31",
    "targetCount": 31,
    "issuedCount": 12,
    "completedCount": 9,
    "completionRatePercent": 29.0,
    "achievementLevel": "LOW_PROGRESS"
  }
}
```

### 9.5 미션 퀵리플라이

모든 요청은 오늘 발행된 미션 카드가 있어야 하며 Request Body는 없습니다.

| 기능 | Method / Path | 설명 |
| --- | --- | --- |
| 다른 미션 보기 | `POST /api/missions/today/regenerate` | 새로운 대체 미션 후보를 조회합니다. |
| 더 쉬운 미션으로 | `POST /api/missions/today/adjust-intensity` | 현재 행동 유형을 유지한 가벼운 강도의 후보를 조회합니다. 이미 가벼운 강도면 `MISSION_ALREADY_LIGHT`(409)를 반환합니다. |
| 이 카테고리 그만 보기 | `POST /api/missions/today/exclude-category` | 현재 카테고리를 당일 추천 제외 목록에 추가합니다. 현재 카드는 유지됩니다. |
| 대체 미션 확정 | `POST /api/missions/today/confirm` | 당일 마지막으로 조회한 대체 후보를 오늘 미션으로 확정합니다. |

- 대체 후보가 없거나 날짜가 지난 경우 `MISSION_CANDIDATE_NOT_FOUND`(409)를 반환합니다. 이 경우 `다른 미션 보기` 또는 `더 쉬운 미션으로`를 다시 호출합니다.

대체 미션 조회 응답 예시:

```json
{
  "status": 200,
  "success": true,
  "message": "다른 미션을 조회했습니다.",
  "data": {
    "title": "지금 가볍게 5분 심호흡하기",
    "description": "편안하게 숨을 깊게 들이쉬고 천천히 내쉬며 심호흡해 보세요.",
    "category": "긴장 완화",
    "estimatedMinutes": 5
  }
}
```

## 10. 홈 대시보드

### 10.1 홈 통합 조회

`GET /api/home`

인증: JWT 필요

홈 화면의 행성 온도계, 오늘 기록 상태, 예보 진행률, 내일 예보, 오늘 미션과 현재 PP 단계를 한 번에 반환합니다.

```json
{
  "status": 200,
  "success": true,
  "message": "홈 대시보드를 조회했습니다.",
  "data": {
    "date": "2026-08-16",
    "nickname": "여행자",
    "planetTemperature": {
      "score": 62,
      "level": "주의",
      "sampleCount": 8
    },
    "todayRecord": {
      "recorded": false,
      "recordId": null,
      "recordCtaRequired": true
    },
    "forecastProgress": {
      "validRecordCount": 3,
      "targetRecordCount": 10,
      "remainingRecordCount": 7,
      "progressPercent": 30,
      "dataBasedForecastReady": false,
      "forecastTransitionReached": false,
      "forecastMode": "ESTIMATED"
    },
    "tomorrowForecast": {
      "riskScore": 62,
      "riskLevel": "보통",
      "source": "COLD_START",
      "aiComment": "내일은 냉난방 노출에 조금 주의해 주세요.",
      "primaryFactors": []
    },
    "todayMission": {
      "id": 10,
      "category": "수분 보충",
      "title": "실내 습도 체크하기",
      "estimatedMinutes": 2,
      "isCompleted": false,
      "streak": 3
    },
    "badge": {
      "stage": 2,
      "name": "탐사자",
      "progressList": []
    }
  }
}
```

- 행성 온도계는 최근 14일 예보 위험도를 사용하며 오늘과 전날 값에 2배 가중치를 적용합니다.
- 위험도 기록이 없으면 `planetTemperature.score`는 `null`, 단계는 `데이터 없음`입니다.
- 온도계 단계는 `0~39 안정`, `40~69 주의`, `70~100 이탈`입니다.
- 내일 예보가 아직 생성되지 않았다면 `tomorrowForecast`는 `null`입니다.
- `forecastMode`는 기록 10건 미만이면 `ESTIMATED`, 10건 이상이면 `DATA_BASED`입니다.
- 오늘 미션이 없으면 기존 미션 생성 로직을 이용해 생성한 뒤 반환합니다.
- PP 승급 팝업은 `badge.stage`를 클라이언트가 마지막 확인 단계와 비교해 한 번만 표시합니다.

## 11. 로컬 실행 환경 변수

MySQL 비밀번호가 없는 경우 IntelliJ 실행 구성에 다음 환경 변수를 설정합니다.

```ini
DB_USERNAME=root;DB_PASSWORD=;JWT_SECRET=skinearth-local-development-secret-key-2026
```

`JWT_SECRET`은 32바이트 이상이어야 합니다. 위 값은 로컬 개발 예시이며 배포 환경에서는 별도의 안전한 값을 사용합니다.

## 12. 권장 연동 테스트 순서

1. 회원가입
2. 로그인 후 JWT 저장
3. 개인화 설문 저장
4. 온보딩 상태 조회
5. 오늘 기록 저장·조회·수정
6. 주간·월간 히스토리 조회
7. 마이페이지 조회
8. 미션이 발행된 경우 오늘 미션 조회·완료·주간·월간 완료율 및 퀵리플라이 조회
9. 테스트 종료 후 필요하면 데이터 초기화
