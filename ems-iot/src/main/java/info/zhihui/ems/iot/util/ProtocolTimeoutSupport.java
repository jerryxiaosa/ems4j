package info.zhihui.ems.iot.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public final class ProtocolTimeoutSupport {

    private ProtocolTimeoutSupport() {
    }

    public static void applyTimeout(CompletableFuture<?> future, long timeoutMillis, Consumer<Throwable> onTimeout) {
        if (future == null || onTimeout == null || timeoutMillis <= 0) {
            return;
        }
        future.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .whenComplete((payload, ex) -> {
                    Throwable cause = unwrap(ex);
                    if (cause instanceof TimeoutException) {
                        onTimeout.accept(cause);
                    }
                });
    }

    private static Throwable unwrap(Throwable ex) {
        if (ex instanceof CompletionException && ex.getCause() != null) {
            return ex.getCause();
        }
        if (ex instanceof ExecutionException && ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }
}
