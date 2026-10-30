package com.tollgate.controller;

import com.tollgate.dto.PlanView;
import com.tollgate.dto.UserSummary;
import com.tollgate.service.DemoDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PlatformController {

    private final DemoDataService demoDataService;

    public PlatformController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "timestamp", Instant.now());
    }

    @GetMapping("/users")
    public List<UserSummary> users() {
        return demoDataService.users();
    }

    @GetMapping("/plans")
    public List<PlanView> plans() {
        return demoDataService.plans();
    }

    @PostMapping("/demo/reset")
    public List<UserSummary> resetDemo() {
        demoDataService.reset();
        return demoDataService.users();
    }
}
