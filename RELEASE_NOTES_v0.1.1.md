# Disender v0.1.1

## Bug Fixes

### 종료 메시지 중복 전송 버그 수정

Spring 컨텍스트 계층 구조에서 `ContextClosedEvent`가 자식 컨텍스트로부터 부모 컨텍스트로 버블링되는 특성으로 인해, 앱 종료 시 shutdown 메시지가 여러 번 전송되는 문제를 수정했습니다.

`DisenderEventListener`가 `ApplicationContextAware`를 구현하도록 변경하여, 자신이 등록된 컨텍스트에서 발생한 이벤트에만 반응하도록 처리했습니다.

## New Features

### Discord 메시지 포함 정보 확대

startup / shutdown 메시지에 운영 환경 파악에 유용한 필드를 추가했습니다.

| 필드 | 설명 | 포함 메시지 |
|---|---|---|
| **PID** | 프로세스 ID | startup, shutdown |
| **Profile** | Spring active profile (없으면 `default`) | startup, shutdown |
| **Uptime** | 앱이 기동된 이후 경과 시간 (`Xh Xm Xs`) | shutdown 전용 |

여러 profile이 활성화된 경우 쉼표로 연결하여 표시합니다 (예: `prod, actuator`).

#### startup 메시지 예시

```
Service    Host        PID     Profile
my-app     server-01   12345   prod
```

#### shutdown 메시지 예시

```
Service    Host        PID     Profile
my-app     server-01   12345   prod
Uptime: 2h 34m 12s
```

## Installation

### Spring Boot 3.x

**Gradle**
```gradle
implementation 'io.github.zaman0806:disender-spring-boot-3-starter:0.1.1'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.zaman0806</groupId>
    <artifactId>disender-spring-boot-3-starter</artifactId>
    <version>0.1.1</version>
</dependency>
```

### Spring Boot 4.0+

**Gradle**
```gradle
implementation 'io.github.zaman0806:disender-spring-boot-4-starter:0.1.1'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.zaman0806</groupId>
    <artifactId>disender-spring-boot-4-starter</artifactId>
    <version>0.1.1</version>
</dependency>
```

## Requirements

- Java 17 이상
- Spring Boot 3.x (boot3 starter) 또는 Spring Boot 4.0+ (boot4 starter)

## License

[MIT License](LICENSE)