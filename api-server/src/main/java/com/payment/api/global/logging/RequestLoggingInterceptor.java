package com.payment.api.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        log.info("[요청] {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME);
        long elapsed = System.currentTimeMillis() - startTime;

        if (ex != null || response.getStatus() >= 500) {
            log.error("[응답] {} {} | status={} | {}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), elapsed);
        } else {
            log.info("[응답] {} {} | status={} | {}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), elapsed);
        }
    }
}
