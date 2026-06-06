# Disender v0.1.0

Spring Boot 애플리케이션이 시작/종료될 때 Discord webhook으로 알림을 자동 발송하는 SDK의 첫 공개 릴리스입니다.

## Features

- **자동 알림**: `ApplicationReadyEvent`와 `ContextClosedEvent`를 수신하여 별도 코드 없이 동작
- **Spring Boot 3.x / 4.0+ 양쪽 지원**: 사용 중인 Boot 버전에 맞는 starter를 선택해 추가
- **장애 격리**: webhook 전송 실패가 앱 기동/종료를 막지 않음 (WARN 로그만 출력)
- **확장 가능**: `DisenderClient` 빈이 노출되어 임의 시점에 커스텀 메시지 전송 가능
- **MSA 친화**: hostname과 `spring.application.name`이 메시지에 자동 포함되어 인스턴스 구분 용이
- **의존성 0 (core)**: `disender-core`는 Spring 의존성 없는 순수 Java 라이브러리

## Installation

### Spring Boot 3.x

**Gradle**
```gradle
implementation 'io.github.zaman0806:disender-spring-boot-3-starter:0.1.0'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.zaman0806</groupId>
    <artifactId>disender-spring-boot-3-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Spring Boot 4.0+

**Gradle**
```gradle
implementation 'io.github.zaman0806:disender-spring-boot-4-starter:0.1.0'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.zaman0806</groupId>
    <artifactId>disender-spring-boot-4-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

`application.yml`에 webhook URL만 설정하면 됩니다:

```yaml
disender:
  webhook-url: https://discord.com/api/webhooks/000000000000000000/xxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

앱을 실행하면 Discord 채널에 시작/종료 embed가 자동 발송됩니다.

## Configuration

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `webhook-url` | (필수) | Discord webhook URL |
| `enabled` | `true` | 전체 활성화 토글 |
| `notify-on-startup` | `true` | 시작 시 알림 발송 |
| `notify-on-shutdown` | `true` | 종료 시 알림 발송 |
| `application-name` | `spring.application.name` | embed에 표시될 서비스 이름 |
| `username` | (없음) | webhook bot 이름 override |
| `timeout` | `5s` | HTTP 연결/읽기 타임아웃 |

## Requirements

- Java 17 이상
- Spring Boot 3.x (boot3 starter) 또는 Spring Boot 4.0+ (boot4 starter)

## Modules

| Module | Description |
| --- | --- |
| `disender-core` | Framework-agnostic Discord webhook client and message model (Spring 의존성 0) |
| `disender-spring-boot-3-starter` | Spring Boot 3.x auto-configuration |
| `disender-spring-boot-4-starter` | Spring Boot 4.0+ auto-configuration |

## Not in This Release

다음 기능은 v0.1.0에 포함되지 않으며 향후 추가 검토 대상입니다:

- 예외/에러 발생 시 자동 알림
- 재시도, 백오프, 비동기 큐잉
- 다중 webhook, Slack 등 타 플랫폼
- 환경 태그(`prod`/`staging`), 커스텀 인스턴스 ID 옵션

## License

[MIT License](LICENSE)
