package info.zhihui.ems.common.utils;

import cn.hutool.core.util.RandomUtil;
import info.zhihui.ems.common.constant.SerialNumberConstant;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author jerryxiaosa
 */
public class SerialNumberGeneratorUtil {

    private static final int UNIQUE_SUFFIX_LENGTH = 8;

    private static final char[] BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private static final BigInteger BASE62_RADIX = BigInteger.valueOf(BASE62_CHARS.length);

    private static final BigInteger ZERO = BigInteger.ZERO;

    /**
     * 生成表编号(yyyyMMdd) + 序号(4位)
     *
     * @param prefix 前缀
     * @return 表编号
     */
    public static String genNoByTime(String prefix) {
        String date = SysDateUtil.toDateTimeString(LocalDateTime.now()).replace("-", "").replace(" ", "").replace(":", "");
        String timeMillis = String.valueOf(System.currentTimeMillis());
        return prefix + date.substring(2) + timeMillis.substring(timeMillis.length() - 3);
    }

    /**
     * 生成全局唯一编号
     *
     * @param prefix 前缀
     * @return 唯一编号
     */
    public static String genUniqueNo(String prefix) {
        String date = SysDateUtil.toDateString(LocalDate.now()).replace("-", "");
        String base62Uuid = encodeUuidToBase62(UUID.randomUUID());
        String suffix = toFixedLengthSuffix(base62Uuid, UNIQUE_SUFFIX_LENGTH);
        return prefix + date.substring(2) + suffix;
    }

    /**
     * 生成订单编号
     *
     * @param orderType 订单类型
     * @return 订单编号
     */
    public static String genOrderSn(Integer orderType) {
        int year = LocalDate.now().getYear();
        long timestamp = Instant.now().getEpochSecond();
        int random = RandomUtil.randomInt(1000); // 0-999

        return String.format("%d%02d%d%03d", year, orderType, timestamp, random);
    }

    /**
     * 生成账户月度订单号（幂等：同账户同月份固定值）
     * 规则：前缀(AM) + 账户ID + 月份(yyyyMM)
     *
     * @param accountId   账户ID
     * @param consumeTime 消费时间，用于确定月份
     * @return 月度订单号
     */
    public static String genMonthlyOrderNo(Integer accountId, LocalDateTime consumeTime) {
        YearMonth ym = YearMonth.from(consumeTime);
        String ymStr = ym.format(DateTimeFormatter.ofPattern("yyyyMM"));
        return SerialNumberConstant.CONSUME_MONTHLY_NO_PREFIX + accountId + ymStr;
    }

    private static String encodeUuidToBase62(UUID uuid) {
        byte[] uuidBytes = ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
        BigInteger number = new BigInteger(1, uuidBytes);
        if (number.equals(ZERO)) {
            return "0";
        }

        StringBuilder builder = new StringBuilder();
        while (number.compareTo(ZERO) > 0) {
            BigInteger[] divideResult = number.divideAndRemainder(BASE62_RADIX);
            builder.append(BASE62_CHARS[divideResult[1].intValue()]);
            number = divideResult[0];
        }
        return builder.reverse().toString();
    }

    private static String toFixedLengthSuffix(String source, int length) {
        if (source.length() >= length) {
            return source.substring(source.length() - length);
        }
        StringBuilder builder = new StringBuilder(length);
        for (int i = source.length(); i < length; i++) {
            builder.append('0');
        }
        builder.append(source);
        return builder.toString();
    }

}
