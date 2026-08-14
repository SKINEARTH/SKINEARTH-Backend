# SKINEARTH-Backend 🌍

피부 컨디션 트래킹 & 예보 서비스 백엔드

## 기술 스택
- Java 21, Spring Boot 4.1.0
- Spring Data JPA, MySQL
- Gradle

## 실행 방법

### 1. DB 생성
로컬 MySQL에 데이터베이스 생성:
​```sql
CREATE DATABASE skinearth;
​```

### 2. 환경변수 설정
IntelliJ 실행 구성 > 환경 변수에 추가:
- `DB_USERNAME` : 본인 MySQL 계정 (보통 root)
- `DB_PASSWORD` : 본인 MySQL 비밀번호
- `JWT_SECRET` : JWT 서명용 32바이트 이상의 비밀값

### 3. 실행
`BackendApplication` 실행

## 도메인 문서

- [구현 완료 API 명세](docs/API.md)
- [User 도메인 협업 가이드](docs/user-domain.md)
- [이메일 인증 협업 가이드](docs/auth.md)
