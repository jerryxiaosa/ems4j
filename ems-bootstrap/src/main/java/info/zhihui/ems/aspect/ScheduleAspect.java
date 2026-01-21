package info.zhihui.ems.aspect;

import info.zhihui.ems.components.lock.core.LockTemplate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

/**
 * 调度切面，对所有 @Scheduled 方法进行加锁控制
 * 确保多节点部署下定时任务单实例执行
 *
 * @author jerryxiaosa
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(prefix = "schedule.aspect", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ScheduleAspect {

    private final LockTemplate lockTemplate;

    public ScheduleAspect(LockTemplate lockTemplate) {
        this.lockTemplate = lockTemplate;
    }

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object aroundScheduled(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getMethod().getName();
        String key = String.format("LOCK:SCHEDULE:%s:%s", className, methodName);

        Lock lock = lockTemplate.getLock(key);
        if (!lock.tryLock()) {
            log.info("[{}:{}] 跳过执行，未获取锁", className, methodName);
            return null;
        }

        try {
            log.info("[{}:{}] 计划任务开始执行", className, methodName);
            Object res = pjp.proceed();
            log.info("[{}:{}] 计划任务执行结束", className, methodName);
            return res;
        } catch (Throwable t) {
            log.error("[{}:{}] 计划任务执行异常", className, methodName, t);
            // 对于调度任务，通常选择吞掉异常避免影响调度线程；如需上报可在此扩展
            return null;
        } finally {
            lock.unlock();
        }

    }
}
