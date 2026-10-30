package com.tollgate.controller;

import com.tollgate.dto.QuotaStatus;
import com.tollgate.dto.UpgradeReceipt;
import com.tollgate.dto.UpgradeRequest;
import com.tollgate.dto.UsageHistory;
import com.tollgate.service.QuotaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private final QuotaService quotaService;

    public QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @GetMapping("/status")
    public QuotaStatus status(@RequestHeader(UserHeader.NAME) String userId) {
        return quotaService.status(userId);
    }

    @GetMapping("/history")
    public UsageHistory history(@RequestHeader(UserHeader.NAME) String userId) {
        return quotaService.history(userId);
    }

    @PostMapping("/upgrade")
    public UpgradeReceipt upgrade(@RequestHeader(UserHeader.NAME) String userId, @Valid @RequestBody UpgradeRequest request) {
        return quotaService.upgrade(userId, request);
    }
}
