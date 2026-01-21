package info.zhihui.ems.common.utils;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionUtil {

    /**
     * 在事务提交后 同步执行
     *
     * @param runnable 待执行的任务
     */
    public static void afterCommitSyncExecute(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
        } else {
            // 没有开启事务时
            runnable.run();
        }
    }

}