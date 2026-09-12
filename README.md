# SKINEARTH-Backend 🌍

<img width="1920" height="1080" alt="0. 표지" src="https://github.com/user-attachments/assets/a5eaaf7a-9c66-4033-a173-42e51c950e03" />

겨울철 사무실 냉난방, 불규칙한 수면과 식사, 반복되는 스트레스처럼 일상 속 생활 환경은 피부 컨디션에 지속적으로 영향을 줍니다.

하지만 기존 피부 관리 서비스는 문제가 발생한 뒤 제품이나 시술을 추천하는 방식에 집중되어 있어, 사용자가 피부 변화의 원인을 파악하고 미리 관리하기는 어렵습니다.

**SKINEARTH**는 사용자의 생활 환경과 피부 상태를 기록하고 누적 데이터를 기반으로 주요 원인을 분석합니다.  
이를 통해 내일의 피부 온도 지수를 예보하고, 사용자에게 맞는 미션을 제공하는 예방 중심의 AI 피부 기록 서비스입니다.

<img width="1920" height="1080" alt="3. 서비스 개요" src="https://github.com/user-attachments/assets/9ad4a246-7a70-4af2-86f0-7f75d850a98a" />

<br>

## 주요 기능

| 도메인 | 기능 |
| --- | --- |
| `auth` | 이메일 기반 회원가입, 로그인, JWT 기반 인증 |
| `user` | 온보딩 설문, 개인화 정보 관리, 마이페이지, 데이터 초기화 |
| `dailyrecord` | 냉난방 노출, 화면 노출, 수면, 스트레스, 식사 규칙성 및 피부 상태 기록 |
| `forecast` | 사용자 기록 기반 피부 온도 지수 분석 및 내일 피부 예보 |
| `mission` | 주요 원인을 기반으로 한 맞춤 미션 추천 및 생성 |
| `badge` | 누적 기록, 연속 기록, 미션 수행 내역을 기반으로 사용자 여정 단계 판정 |
| `history` | 피부 온도 지수 추이, 주요 원인 변화, 미션 완료율 조회 |
| `home` | 오늘의 기록, 예보, 미션 등 홈 화면에 필요한 데이터 제공 |

<br>

## 팀 구성

| 이름 | 담당 |
| --- | --- |
| 강지윤 | 예보(통계·AI 코멘트), 미션(AI 미션 생성), 원인 타임라인·뱃지, 배포 |
| 박수빈 | 로그인·회원가입, 온보딩 설문, 일일 기록, 홈, 히스토리, 마이페이지 |

<br>

## 기술 스택

| Category | Stack |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| ORM | Spring Data JPA |
| Database | MySQL |
| Security | Spring Security, JWT |
| AI | Gemini API |
| Build | Gradle |

<br>

## 시스템 아키텍처

```text
┌──────────┐
│  Client  │
└────┬─────┘
     │ REST API / JWT
     ▼
┌───────────────────────────────┐
│      Spring Boot Server       │
│                               │
│  Spring Security / JWT        │
│  Business Logic               │
│  Spring Data JPA              │
└──────────┬───────────┬────────┘
           │           │
           ▼           ▼
       ┌───────┐   ┌────────────┐
       │ MySQL │   │ Gemini API │
       └───────┘   └────────────┘
```

<br>

## 프로젝트 구조

```text
com.skinearth.backend
├── auth           # 로그인, 회원가입, JWT 인증
├── user           # 온보딩 설문, 개인화, 마이페이지, 데이터 리셋
├── dailyrecord    # 일일 기록
├── forecast       # 피부 온도 지수 분석 및 내일 예보
├── mission        # 맞춤 미션 추천 및 생성
├── badge          # 사용자 여정 단계 판정
├── history        # 지수 추이, 원인 변화, 미션 완료율
├── home           # 홈 화면 데이터
└── common         # 공통 응답, 예외 처리, 설정
```

<br>

## API 문서

- [API 명세](docs/API.md)
- [User 도메인 협업 가이드](docs/user-domain.md)
- [Auth 도메인 협업 가이드](docs/auth.md)

<br>

## 로컬 실행

### 1. Repository Clone

```bash
git clone https://github.com/SKINEARTH/SKINEARTH-Backend.git
cd SKINEARTH-Backend
```

### 2. Database 생성

로컬 MySQL에 데이터베이스를 생성합니다.

```sql
CREATE DATABASE skinearth;
```

### 3. 환경변수 설정

다음 환경변수를 설정합니다.

```env
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
GEMINI_API_KEY=
```

| 환경변수 | 설명 |
| --- | --- |
| `DB_USERNAME` | MySQL 사용자 계정 |
| `DB_PASSWORD` | MySQL 비밀번호 |
| `JWT_SECRET` | JWT 서명에 사용하는 Secret |
| `GEMINI_API_KEY` | 예보 코멘트 및 미션 생성에 사용하는 Gemini API Key |

Gemini API Key는 [Google AI Studio](https://aistudio.google.com/apikey)에서 발급할 수 있습니다.

### 4. 실행

```bash
./gradlew bootRun
```

또는 IntelliJ에서 `BackendApplication`을 실행합니다.
