## Dicoshot v0.4.0

Spring Boot 3 / 4용으로 분리되어 있던 두 starter를 단일 starter로 통합한 릴리스입니다.

### 변경 사항

- **starter 통합**: `dicoshot-spring-boot-3-starter` + `dicoshot-spring-boot-4-starter` → `dicoshot-spring-boot-starter`
  - 두 starter는 패키지명(`io.dicoshot.boot3` vs `io.dicoshot.boot4`)만 다른 동일 코드였습니다.
  - 통신에 `RestClient`(Boot 3.2+ / 4.x 공통 API)만 사용하므로 버전별 분기가 없어 분리할 이유가 없었습니다.
  - 통합 패키지: `io.dicoshot.spring`
- 기존 `dicoshot-spring-boot-3-starter` / `dicoshot-spring-boot-4-starter`는 **deprecated**됩니다 (Maven Central에 배포된 0.3.0 이하 artifact는 그대로 유지).

### 설치

Spring Boot 3.2+ 와 4.x 모두 동일한 starter를 사용합니다.

```gradle
implementation 'io.github.dicoshot:dicoshot-spring-boot-starter:0.4.0'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.dicoshot</groupId>
    <artifactId>dicoshot-spring-boot-starter</artifactId>
    <version>0.4.0</version>
</dependency>
```

### 빠른 시작

```yaml
dicoshot:
  webhook-url: https://discord.com/api/webhooks/.../...
```

의존성과 위 설정만으로 애플리케이션 시작/종료 시 Discord 채널에 알림이 발송됩니다.

### 이전 버전 사용자 안내

0.3.0(`dicoshot-spring-boot-3-starter` / `dicoshot-spring-boot-4-starter`)을 사용하던 프로젝트는 의존성 좌표만 교체하면 됩니다.

- `io.github.dicoshot:dicoshot-spring-boot-3-starter` → `io.github.dicoshot:dicoshot-spring-boot-starter`
- `io.github.dicoshot:dicoshot-spring-boot-4-starter` → `io.github.dicoshot:dicoshot-spring-boot-starter`

import 경로(`io.dicoshot.*`), 클래스명, 설정 키(`dicoshot.*`)는 변경되지 않습니다.

### 요구사항

- Java 17 이상
- Spring Boot 3.2 이상 (3.2+ 또는 4.x). `RestClient` 도입 이전인 3.0~3.1은 지원하지 않습니다.

### 라이선스

[MIT License](LICENSE) © 2026 ZaMan0806
