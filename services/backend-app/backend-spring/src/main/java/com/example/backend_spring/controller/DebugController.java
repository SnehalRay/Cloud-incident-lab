package com.example.backend_spring.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    // Endpoint to intentionally crash the backend for testing purposes
    @GetMapping("/crash")
    public void crash() {
        log.error("intentional_crash triggered via /api/debug/crash");
        throw new RuntimeException("Intentional crash triggered via /api/debug/crash");
    }
}
