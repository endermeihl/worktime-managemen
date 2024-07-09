package com.nr.worktime.interceptor;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter = RateLimiter.create(10); // 10 tokens per second
    private static final int TOO_MANY_REQUESTS = 429; // Custom HTTP status code for Too Many Requests

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // Rate limiting
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(TOO_MANY_REQUESTS);
            response.getWriter().write("Too many requests");
            return false;
        }

        // Check for invalid characters
        String queryString = request.getQueryString();
        if (queryString != null && !isValidQueryString(queryString)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid characters in request");
            return false;
        }

        return true;
    }

    private boolean isValidQueryString(String queryString) {
        // Example validation: reject non-ASCII characters
        return StandardCharsets.US_ASCII.newEncoder().canEncode(queryString);
    }
}
