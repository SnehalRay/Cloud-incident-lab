package com.example.backend_spring.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    /**
     * This filter logs the incoming requests and their response status along with the time taken to process the request. It uses Spring's OncePerRequestFilter to ensure that it is executed only once per request. The log includes the endpoint, HTTP method, response status, and duration in milliseconds. This can be useful for monitoring and debugging purposes.
     * This is basically the middleware
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis(); // the current time in milliseconds before processing the request, we will use this to calculate the duration of the request processing
        try {
            chain.doFilter(request, response); // continue the filter chain, this will eventually lead to the controller handling the request and generating a response
        } finally {
            long duration = System.currentTimeMillis() - start; // calculate the duration of the request processing by subtracting the start time from the current time after the request has been processed
            log.info("service=backend endpoint={} method={} status={} duration_ms={}",
                    request.getRequestURI(), request.getMethod(), response.getStatus(), duration); // log the endpoint, HTTP method, response status, and duration of the request processing in milliseconds. This log can be used for monitoring and debugging purposes to understand the performance of the backend service and identify any potential issues with specific endpoints or methods.
        }
    }
}
