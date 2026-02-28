package info.zhihui.ems.web.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 离线时长工具类。
 */
public final class OfflineDurationUtil {

    private OfflineDurationUtil() {
    }

    /**
     * 格式化离线时长。
     *
     * @param isOnline       当前是否在线
     * @param lastOnlineTime 最近一次确认在线时间
     * @return 离线时长展示值；在线或无法计算时返回 null
     */
    public static String format(Boolean isOnline, LocalDateTime lastOnlineTime) {
        return format(isOnline, lastOnlineTime, LocalDateTime.now());
    }

    private static String format(Boolean isOnline, LocalDateTime lastOnlineTime, LocalDateTime currentTime) {
        if (Boolean.TRUE.equals(isOnline) || lastOnlineTime == null || lastOnlineTime.isAfter(currentTime)) {
            return null;
        }

        long offlineMinuteCount = Math.max(1L, Duration.between(lastOnlineTime, currentTime).toMinutes());
        long offlineDayCount = offlineMinuteCount / (24 * 60);
        if (offlineDayCount >= 1) {
            return offlineDayCount + "天";
        }

        long offlineHourCount = offlineMinuteCount / 60;
        if (offlineHourCount >= 1) {
            return offlineHourCount + "小时";
        }

        return offlineMinuteCount + "分钟";
    }
}
