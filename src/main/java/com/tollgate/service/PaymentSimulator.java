package com.tollgate.service;

import com.tollgate.exception.PaymentDeclinedException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentSimulator {

    private static final String DECLINED_SUFFIX = "0000";

    public PaymentConfirmation charge(String cardHolder, String cardNumber, BigDecimal amountUsd) {
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        if (DECLINED_SUFFIX.equals(last4)) {
            throw new PaymentDeclinedException("The card ending in " + last4 + " was declined by the simulated bank");
        }
        String transactionId = "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return new PaymentConfirmation(transactionId, cardHolder.trim(), last4, amountUsd, Instant.now());
    }

    public record PaymentConfirmation(String transactionId, String cardHolder, String cardLast4, BigDecimal amountUsd, Instant paidAt) {
    }
}
