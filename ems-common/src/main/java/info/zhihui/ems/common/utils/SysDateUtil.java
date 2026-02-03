package info.zhihui.ems.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 *
 * @author jerryxiaosa
 */
public class SysDateUtil {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER_T = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER_ZONE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final DateTimeFormatter DATETIME_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER_SSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER_NUMERIC = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_CHINESE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    private static final DateTimeFormatter DATETIME_MICROSECOND_FORMATTER_ZONE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static final String ZONE_SHANGHAI = "Asia/Shanghai";

    public static String toDateTimeString(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATETIME_FORMATTER.format(dateTime);
    }

    public static String toDateTimeZoneString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        // 将LocalDateTime转换为带时区的ZonedDateTime（假设需要东八区时间）
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of(ZONE_SHANGHAI));
        return DATETIME_FORMATTER_ZONE.format(zonedDateTime);
    }

    public static LocalDateTime toDateTime(String dateTimeString) {
        return StringUtils.isBlank(dateTimeString) ? null : LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
    }

    public static LocalDateTime toDateTimeWithT(String dateTimeString) {
        return StringUtils.isBlank(dateTimeString) ? null : LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER_T);
    }

    public static LocalDateTime toDateTimeWithMillis(String dateTimeString) {
        return StringUtils.isBlank(dateTimeString) ? null : LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER_SSS);
    }

    public static String toDateString(LocalDate date) {
        return date == null ? "" : DATE_FORMATTER.format(date);
    }

    public static String toDateString(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_FORMATTER.format(dateTime);
    }

    public static Integer toDateInt(LocalDate date) {
        return date == null ? null : Integer.parseInt(DATE_FORMATTER_NUMERIC.format(date));
    }

    public static String toDateTimeMinuteString(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATETIME_MINUTE_FORMATTER.format(dateTime);
    }

    public static String toTimeMinuteString(LocalTime time) {
        return time == null ? "" : TIME_MINUTE_FORMATTER.format(time);
    }

    public static String toTimeMinuteString(LocalDateTime dateTime) {
        return dateTime == null ? "" : TIME_MINUTE_FORMATTER.format(dateTime);
    }

    public static LocalDateTime toDateTimeMinute(String dateTimeString) {
        return StringUtils.isBlank(dateTimeString) ? null : LocalDateTime.parse(dateTimeString, DATETIME_MINUTE_FORMATTER);
    }

    public static LocalDate toDate(String dateString) {
        return StringUtils.isBlank(dateString) ? null : LocalDate.parse(dateString, DATE_FORMATTER);
    }

    public static LocalDate toDate(Integer dateInt) {
        return dateInt == null ? null : LocalDate.parse(dateInt.toString(), DATE_FORMATTER_NUMERIC);
    }

    public static String toYearMonthString(LocalDate date) {
        return date == null ? "" : YEAR_MONTH_FORMATTER.format(date);
    }

    public static Integer toYearMonthInt(LocalDate date) {
        return date == null ? null : Integer.parseInt(YEAR_MONTH_FORMATTER.format(date).replace("-", ""));
    }

    // 形如：2024-08
    public static LocalDate toYearMonth(String dateString) {
        if (StringUtils.isBlank(dateString) || dateString.length() != 7) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM");
        }
        return LocalDate.of(Integer.parseInt(dateString.substring(0, 4)), Integer.parseInt(dateString.substring(5)), 1);
    }

    public static String toNumericString(LocalDate date) {
        return date == null ? "" : DATE_FORMATTER_NUMERIC.format(date);
    }

    public static String toChineseDateString(LocalDate date) {
        return date == null ? "" : DATE_CHINESE_FORMATTER.format(date);
    }

    public static String toOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("localDateTime must not be null");
        }
        // 将LocalDateTime转换为带时区的ZonedDateTime（假设需要东八区时间）
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(ZONE_SHANGHAI));

        return zonedDateTime.format(DATETIME_MICROSECOND_FORMATTER_ZONE);
    }

    public static String toDateString(String dateTimeString) {
        if (StringUtils.isBlank(dateTimeString)) {
            throw new IllegalArgumentException("dateTimeString must not be blank");
        }

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        return dateTime.format(DATE_FORMATTER);
    }

    public static String dateTimeZoneToDateString(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, formatter);
        LocalDateTime dateTime = zonedDateTime.toLocalDateTime();

        return dateTime.format(DATETIME_FORMATTER);
    }

}
