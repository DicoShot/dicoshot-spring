# Disender

Spring Boot 애플리케이션이 시작/종료될 때 Discord 채널로 알림을 자동 발송하는 SDK입니다. 의존성 추가와 `application.yml` 설정만으로 동작합니다.

## 특징

- **자동 알림**: `ApplicationReadyEvent`와 `ContextClosedEvent`를 수신하여 별도 코드 없이 동작
- **양쪽 버전 지원**: Spring Boot 3.x와 Spring Boot 4.0+ 각각에 대한 starter 제공
- **장애 격리**: webhook 전송 실패가 앱 기동/종료를 막지 않음 (WARN 로그만 출력)
- **확장 가능**: `DisenderClient` 빈이 노출되어 임의 시점에 커스텀 메시지 전송 가능
- **MSA 친화**: hostname과 `spring.application.name`이 메시지에 자동 포함되어 인스턴스 구분 용이

## 모듈 구성

| 모듈 | 설명 |
| --- | --- |
| `disender-core` | Spring 의존성 없는 순수 Java. 메시지 모델, JSON 직렬화, `DisenderClient` 인터페이스 |
| `disender-spring-boot-3-starter` | Spring Boot 3.x 전용 auto-configuration |
| `disender-spring-boot-4-starter` | Spring Boot 4.0+ 전용 auto-configuration |

## 설치

### Spring Boot 3.x

**Gradle**
```gradle
implementation 'io.github.dicoshot:disender-spring-boot-3-starter:0.2.0'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.dicoshot</groupId>
    <artifactId>disender-spring-boot-3-starter</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Spring Boot 4.0+

**Gradle**
```gradle
implementation 'io.github.dicoshot:disender-spring-boot-4-starter:0.2.0'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.dicoshot</groupId>
    <artifactId>disender-spring-boot-4-starter</artifactId>
    <version>0.2.0</version>
</dependency>
```

> Maven coordinate의 group은 `io.github.dicoshot`이지만 **Java import 경로는 그대로 `io.disender.*`** 입니다. Maven group과 Java package는 무관합니다.
>
> 0.1.x는 `io.github.zaman0806` group으로 배포되었습니다. 0.2.0부터 `io.github.dicoshot`으로 이전되었으니 의존성 좌표를 갱신하세요.

## 빠른 시작

`application.yml`에 webhook URL만 설정하면 됩니다.

```yaml
disender:
  webhook-url: https://discord.com/api/webhooks/000000000000000000/xxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

앱을 실행하면 Discord 채널에 다음과 같은 embed가 도착합니다.

- **시작 시**: 녹색 embed, 제목 "Application started", 서비스 이름과 호스트명 포함
- **종료 시**: 빨강 embed, 제목 "Application shutting down"

## 설정

모든 옵션은 `disender.*` prefix 아래 위치합니다.

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `webhook-url` | (필수) | Discord webhook URL. 미설정 시 SDK는 자동 비활성화 |
| `enabled` | `true` | 전체 활성화 토글 |
| `notify-on-startup` | `true` | 시작 시 알림 발송 여부 |
| `notify-on-shutdown` | `true` | 종료 시 알림 발송 여부 |
| `application-name` | `spring.application.name` | embed에 표시될 서비스 이름. 미설정 시 Spring 표준 프로퍼티 사용 |
| `username` | (없음) | webhook bot의 표시 이름을 override |
| `timeout` | `5s` | HTTP 연결/읽기 타임아웃 (`Duration` 형식) |

### 예시: 시작 시에만 알림

```yaml
disender:
  webhook-url: ${DISCORD_WEBHOOK_URL}
  notify-on-shutdown: false
  application-name: order-service
  username: Disender Bot
  timeout: 3s
```

### 예시: 환경 변수로 webhook URL 주입

```yaml
disender:
  webhook-url: ${DISCORD_WEBHOOK_URL:}
```

값이 비어있으면 starter가 자동으로 비활성화되어 로컬 개발 환경에서 webhook 미설정으로 인한 오류가 발생하지 않습니다.

## 커스텀 메시지 발송

SDK가 등록한 `DisenderClient` 빈을 주입받아 임의 시점에 메시지를 보낼 수 있습니다.

```java
import io.disender.core.DisenderClient;
import io.disender.core.message.DiscordEmbed;
import io.disender.core.message.DiscordMessage;
import org.springframework.stereotype.Component;

@Component
public class DeployNotifier {

    private final DisenderClient disender;

    public DeployNotifier(DisenderClient disender) {
        this.disender = disender;
    }

    public void notifyDeploy(String version) {
        DiscordMessage msg = DiscordMessage.builder()
                .addEmbed(DiscordEmbed.builder()
                        .title("Deploy completed")
                        .description("Version " + version + " is live")
                        .color(0x3498DB)
                        .build())
                .build();
        disender.send(msg);
    }
}
```

## 동작 방식

1. starter는 `disender.webhook-url`이 설정되어 있고 `disender.enabled`가 `false`가 아닐 때만 활성화됩니다.
2. `DisenderAutoConfiguration`이 다음 빈을 등록합니다.
   - `DisenderProperties`: 설정 값 컨테이너
   - `RestClient`: 타임아웃이 적용된 HTTP 클라이언트
   - `DisenderClient`: webhook 전송 구현체
   - `MessageFactory`: startup/shutdown embed 생성기
   - `DisenderEventListener`: 이벤트 구독자
3. `ApplicationReadyEvent` 수신 시 startup 메시지를, `ContextClosedEvent` 수신 시 shutdown 메시지를 발송합니다.
4. webhook 호출이 실패해도 예외는 삼키고 WARN 로그만 남기므로 앱 기동/종료가 영향을 받지 않습니다.

모든 빈은 `@ConditionalOnMissingBean`으로 등록되므로 사용자가 동일한 타입의 빈을 등록하면 SDK 기본값을 override할 수 있습니다.

## 멀티모듈 / MSA 환경

- **단일 앱의 Gradle 멀티모듈**: `bootJar`를 생성하는 모듈에만 starter 의존성을 추가하세요. `ApplicationReadyEvent`는 컨텍스트 전체 준비 후 1회만 발생하므로 알림도 1회 발송됩니다.
- **MSA (여러 독립 서비스)**: 각 서비스가 starter를 독립적으로 사용합니다. 메시지에 `spring.application.name`과 hostname이 자동 포함되어 어느 서비스의 어느 인스턴스인지 식별 가능합니다.
- **알림 폭주 방지**: K8s 등에서 Pod 재시작이 잦은 경우 `disender.notify-on-startup: false`로 시작 알림만 끄거나, `disender.enabled: false`로 전체 비활성화할 수 있습니다.

## 요구사항

- Java 17 이상
- Spring Boot 3.x (boot3 starter) 또는 Spring Boot 4.0+ (boot4 starter)

## 빌드

```bash
./gradlew build
```

테스트만 실행하려면:

```bash
./gradlew test
```

특정 모듈만:

```bash
./gradlew :disender-core:test
./gradlew :disender-spring-boot-3-starter:test
./gradlew :disender-spring-boot-4-starter:test
```

## 범위 외 (현재 버전)

다음 기능은 v0.1에 포함되지 않습니다.

- 예외/에러 발생 시 자동 알림 (`@ControllerAdvice` 등 hook)
- 재시도, 백오프, 비동기 큐잉
- 다중 webhook 또는 Slack 등 타 플랫폼
- 환경 태그(`prod`/`staging`), 커스텀 인스턴스 ID 옵션

필요 시 `DisenderClient` 빈을 직접 사용하여 애플리케이션 코드에서 구현할 수 있습니다.

## 라이선스

[MIT License](LICENSE) © 2026 ZaMan0806
