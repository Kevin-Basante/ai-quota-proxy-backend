package com.tollgate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "ai-quota-proxy-backend");
        body.put("status", "UP");
        body.put("endpoints", List.of(
                "POST /api/ai/generate",
                "GET /api/quota/status",
                "GET /api/quota/history",
                "POST /api/quota/upgrade",
                "GET /api/users",
                "GET /api/plans",
                "GET /api/health"
        ));
        return body;
    }
}
