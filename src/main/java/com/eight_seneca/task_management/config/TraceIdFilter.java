package com.eight_seneca.task_management.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException {

        try {
            MDC.put("http_method", request.getMethod());
            MDC.put("http_uri", request.getRequestURI());
            String traceId = UUID.randomUUID().toString();
//            MDC.put("traceId", traceId);

            filterChain.doFilter(request, response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } finally {
            MDC.clear(); // ⚠️ rất quan trọng
        }
    }
}
