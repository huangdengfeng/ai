package com.seezoon.infrastructure.utils;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import java.util.concurrent.ExecutorService;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

public class OtelUtils {

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";

    public static <T> void setAttribute(@Nullable AttributeKey<T> key, T value) {
        Span.current().setAttribute(key, value);
    }

    public static String getTraceId() {
        return Span.current().getSpanContext().getTraceId();
    }

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static String getSpanId() {
        return Span.current().getSpanContext().getSpanId();
    }

    public static void setSpanId(String spanId) {
        MDC.put(SPAN_ID, spanId);
    }

    public static ExecutorService wrap(ExecutorService executorService) {
        return Context.taskWrapping(executorService);
    }
}
