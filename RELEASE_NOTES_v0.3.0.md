## Dicoshot v0.3.0

DicoShot 조직으로의 이전과 함께 `disender` 네이밍을 `dicoshot`으로 전면 변경한 첫 릴리스입니다.

### 변경 사항

- **네이밍 전면 변경**: `disender` → `dicoshot`
  - Maven group: `io.github.dicoshot`
  - Artifact: `dicoshot-core`, `dicoshot-spring-boot-3-starter`, `dicoshot-spring-boot-4-starter`
  - 패키지: `io.dicoshot.*`
  - 클래스: `DicoshotClient`, `DicoshotProperties`, `DicoshotAutoConfiguration` 등
  - 설정 prefix: `dicoshot.*`
- DicoShot 조직 레포(`DicoShot/dicoshot-spring`)로 이전

### 설치

**Spring Boot 3.x**
```gradle
implementation 'io.github.dicoshot:dicoshot-spring-boot-3-starter:0.3.0'
```

**Spring Boot 4.0+**
```gradle
implementation 'io.github.dicoshot:dicoshot-spring-boot-4-starter:0.3.0'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.dicoshot</groupId>
    <artifactId>dicoshot-spring-boot-3-starter</artifactId>
    <version>0.3.0</version>
</dependency>
```

### 빠른 시작

```yaml
dicoshot:
  webhook-url: https://discord.com/api/webhooks/.../...
```

의존성과 위 설정만으로 애플리케이션 시작/종료 시 Discord 채널에 알림이 발송됩니다.

### 이전 버전 사용자 안내

0.1.x(`io.github.zaman0806:disender-*`)를 사용하던 프로젝트는 다음을 갱신해야 합니다.

- 의존성 좌표: `io.github.dicoshot:dicoshot-*:0.3.0`
- import 경로: `io.disender.*` → `io.dicoshot.*`
- 클래스명: `Disender*` → `Dicoshot*`
- 설정 키: `disender.*` → `dicoshot.*`

### 요구사항

- Java 17 이상
- Spring Boot 3.x (boot3 starter) 또는 Spring Boot 4.0+ (boot4 starter)

### 라이선스

[MIT License](LICENSE) © 2026 ZaMan0806