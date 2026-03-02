package info.zhihui.ems.common.utils;

import cn.hutool.core.util.RandomUtil;
import info.zhihui.ems.common.constant.SerialNumberConstant;

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
        return prefix + date.substring(2) + "-" + UUID.randomUUID().toString().replace("-", "");
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

}
