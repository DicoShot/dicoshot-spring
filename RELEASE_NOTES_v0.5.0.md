## Dicoshot v0.5.0

이벤트가 발생하는 지점마다 메시지 형식만 정해서 보낼 수 있도록 `@DicoshotNotify` 어노테이션을 추가한 릴리스입니다.

### 새 기능

- **`@DicoshotNotify` 어노테이션**: 메서드에 붙이면, 그 메서드가 반환한 `DiscordMessage`가 정상 반환 직후 자동으로 Discord에 전송됩니다. 알림이 필요한 지점마다 메시지 형식만 정해서 `return`하면 됩니다.

  ```java
  @Component
  public class OrderNotifier {

      @DicoshotNotify
      public DiscordMessage onOrderPlaced(Order order) {
          return DiscordMessage.builder()
                  .addEmbed(DiscordEmbed.builder()
                          .title("New order #" + order.getId())
                          .color(0x2ECC71)
                          .addField("Amount", order.getAmount() + " KRW", true)
                          .build())
                  .build();
      }
  }
  ```

  - **조건부 발송**: `null`을 반환하면 전송하지 않습니다 (`return shouldNotify ? message : null;`).
  - **반환 타입**: `DiscordMessage`를 반환해야 하며, 그 외 타입은 무시됩니다.
  - **실패 격리**: webhook 전송이 실패해도 WARN 로그만 남기며, 어노테이션이 붙은 메서드의 호출 흐름에는 영향을 주지 않습니다. 메서드가 예외를 던지면 전송하지 않습니다.
  - Spring AOP 프록시 기반이므로 같은 클래스 내부의 self-invocation에는 적용되지 않습니다.

  기존처럼 `DicoshotClient` 빈을 직접 주입받아 임의 시점에 메시지를 보내는 방식도 그대로 사용할 수 있습니다.

### 설치

`spring-aop`와 `aspectjweaver`가 전이 의존성으로 포함되어 있으므로 별도 추가 없이 아래 하나만 추가하면 됩니다.

```gradle
implementation 'io.github.dicoshot:dicoshot-spring-boot-starter:0.5.0'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.dicoshot</groupId>
    <artifactId>dicoshot-spring-boot-starter</artifactId>
    <version>0.5.0</version>
</dependency>
```

### 이전 버전 사용자 안내

0.4.0에서 0.5.0으로의 업그레이드는 호환됩니다. 의존성 버전만 교체하면 되며, 기존 startup/shutdown 알림과 `DicoshotClient` 사용 방식은 변경되지 않습니다. `@DicoshotNotify`는 새로 추가된 선택 기능입니다.

### 요구사항

- Java 17 이상
- Spring Boot 3.2 이상 (3.2+ 또는 4.x). `RestClient` 도입 이전인 3.0~3.1은 지원하지 않습니다.

### 라이선스

[MIT License](LICENSE) © 2026 ZaMan0806