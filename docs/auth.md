# 이메일 인증 협업 가이드

## 인증 흐름

1. `POST /api/auth/signup`으로 이메일, 비밀번호, 약관 동의 정보를 저장합니다.
2. 개인화 설문의 닉네임, 현재 상태, 피부 고민은 가입 후 별도 API에서 입력합니다.
3. `POST /api/auth/login` 성공 시 1시간 유효한 JWT Access Token을 발급합니다.
4. 인증이 필요한 API는 `Authorization: Bearer {token}` 헤더를 전달합니다.

## 환경 변수

서버 실행 환경에 32바이트 이상의 임의 문자열을 설정해야 합니다.

```text
JWT_SECRET=로컬과_배포환경에서_각각_관리하는_32바이트_이상의_비밀값
```

실제 값은 `application.yml`, 문서, GitHub에 커밋하지 않습니다.

## API 요청 예시

### 회원가입

```json
{
  "email": "user@example.com",
  "password": "password123!",
  "passwordConfirm": "password123!",
  "serviceTermsAgreed": true,
  "sensitiveDataAgreed": true,
  "researchDataAgreed": false
}
```

### 로그인

```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

## 다른 도메인 연동

- JWT의 `sub`에는 User ID가 문자열로 저장됩니다.
- 인증된 API에서는 JWT의 subject를 사용해 현재 사용자 ID를 얻습니다.
- 클라이언트가 임의의 `userId`를 보내도록 새 API를 설계하지 않습니다.
- 기존 Forecast 등 `userId`를 직접 받는 API의 전환은 별도 연동 이슈에서 진행합니다.

## 로그아웃

현재 인증은 서버 세션이나 Refresh Token을 저장하지 않는 Stateless JWT 방식입니다.
로그아웃 시 클라이언트가 보관 중인 Access Token을 삭제합니다. 서버 강제 로그아웃과 토큰 차단 목록은 현재 범위에 포함하지 않습니다.
