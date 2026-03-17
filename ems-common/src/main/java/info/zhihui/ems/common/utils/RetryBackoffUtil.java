package info.zhihui.ems.common.utils;

import java.time.LocalDateTime;

/**
 * 重试退避工具
 *
 * @author jerryxiaosa
 */
public final class RetryBackoffUtil {

    private static final long BASE_DELAY_SECONDS = 60L;
    private static final long MAX_DELAY_SECONDS = 3600L;

    private RetryBackoffUtil() {
    }

    public static long calculateDelaySeconds(Integer executeTimes) {
        int safeExecuteTimes = executeTimes == null ? 0 : Math.max(executeTimes, 0);
        if (safeExecuteTimes <= 1) {
            return BASE_DELAY_SECONDS;
        }

        long delaySeconds = BASE_DELAY_SECONDS;
        for (int index = 1; index < safeExecuteTimes; index++) {
            if (delaySeconds >= MAX_DELAY_SECONDS) {
                return MAX_DELAY_SECONDS;
            }
            delaySeconds = Math.min(delaySeconds * 2, MAX_DELAY_SECONDS);
        }
        return delaySeconds;
    }

    public static boolean shouldRetryNow(LocalDateTime lastExecuteTime, Integer executeTimes, LocalDateTime now) {
        if (lastExecuteTime == null || now == null) {
            return true;
        }
        LocalDateTime nextRetryTime = lastExecuteTime.plusSeconds(calculateDelaySeconds(executeTimes));
        return !nextRetryTime.isAfter(now);
    }
}
