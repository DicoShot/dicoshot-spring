package io.dicoshot.spring;

import io.dicoshot.core.DicoshotClient;
import io.dicoshot.core.message.DiscordMessage;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends the {@link DiscordMessage} returned by a {@link DicoshotNotify}-annotated method.
 *
 * <p>Runs after the method returns normally. A {@code null} return is treated as "nothing to
 * send" so methods can decide at runtime whether to notify. A send failure is logged and
 * swallowed so it never breaks the annotated method's caller.
 */
@Aspect
public class DicoshotNotifyAspect {

    private static final Logger log = LoggerFactory.getLogger(DicoshotNotifyAspect.class);

    private final DicoshotClient client;

    public DicoshotNotifyAspect(DicoshotClient client) {
        this.client = client;
    }

    @AfterReturning(
            pointcut = "@annotation(io.dicoshot.spring.DicoshotNotify)",
            returning = "result")
    public void send(JoinPoint joinPoint, Object result) {
        if (!(result instanceof DiscordMessage message)) {
            return;
        }
        try {
            client.send(message);
        } catch (RuntimeException e) {
            log.warn("Dicoshot @DicoshotNotify send failed for {}: {}",
                    joinPoint.getSignature().toShortString(), e.getMessage());
        }
    }
}