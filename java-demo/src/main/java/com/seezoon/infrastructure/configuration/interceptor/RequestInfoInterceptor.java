package com.seezoon.infrastructure.configuration.interceptor;


import com.seezoon.infrastructure.utils.OtelUtils;
import io.opentelemetry.api.common.AttributeKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 打印请求中的RequestId
 */
@Slf4j
public class RequestInfoInterceptor implements HandlerInterceptor {

    private static final String REQ_ID = "X-Request-Id";
    private static final AttributeKey ATTR_REQUEST_ID = AttributeKey.stringKey("request_id");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = request.getHeader(REQ_ID);
        if (StringUtils.isNotEmpty(requestId)) {
            log.debug("X-Request-Id:{}", requestId);
        }
        OtelUtils.setAttribute(ATTR_REQUEST_ID, requestId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
    }
}
