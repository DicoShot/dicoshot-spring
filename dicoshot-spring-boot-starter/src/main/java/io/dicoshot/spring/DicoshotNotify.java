package io.dicoshot.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method whose returned {@link io.dicoshot.core.message.DiscordMessage} should be
 * sent to Discord automatically after the method returns normally.
 *
 * <p>The annotated method builds and returns the message it wants to send:
 * <pre>{@code
 * @DicoshotNotify
 * public DiscordMessage onOrderPlaced(Order order) {
 *     return DiscordMessage.builder()
 *             .addEmbed(DiscordEmbed.builder()
 *                     .title("New order #" + order.getId())
 *                     .color(0x2ECC71)
 *                     .addField("Amount", order.getAmount() + " KRW", true)
 *                     .build())
 *             .build();
 * }
 * }</pre>
 *
 * <p>Returning {@code null} skips sending — this makes conditional notifications possible
 * ({@code return shouldNotify ? message : null;}). If the method throws, nothing is sent.
 *
 * <p>Sending happens after the method returns and never propagates a failure: if the webhook
 * call fails, a warning is logged and the caller's flow is unaffected. The aspect is only
 * active when Spring AOP is on the classpath.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DicoshotNotify {
}