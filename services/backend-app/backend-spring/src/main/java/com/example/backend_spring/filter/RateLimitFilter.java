package com.example.backend_spring.filter;

import java.io.IOException;

import com.example.backend_spring.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException{
        // only apply rate limiting to POST /api/items — all other endpoints pass through freely
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/api/items".equals(request.getRequestURI())){

            // read the instance ID from the header — each frontend instance sets this to identify itself
            // if missing, treat it as "unknown" so it still gets rate limited
            String clientId = request.getHeader("X-Instance-ID");
            if (clientId == null || clientId.isBlank()) {
                clientId = "unknown";
            }

            if (!rateLimiterService.isAllowed(clientId)){
                // limit exceeded — log it, queue a violation job for the worker, and reject with 429
                log.warn("rate_limit_exceeded instance_id={} endpoint={}", clientId, request.getRequestURI());
                rateLimiterService.pushViolationJob(clientId, request.getRequestURI());
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too Many Requests\"}");
                return;
            }
        }

        // request is either not rate-limited or within the limit — pass it to the next filter/controller
        chain.doFilter(request, response);
    }

}
