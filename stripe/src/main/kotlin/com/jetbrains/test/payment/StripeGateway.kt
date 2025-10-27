package com.jetbrains.test.payment

import com.jetbrains.test.PaymentGateway
import com.jetbrains.test.PaymentStatus
import org.slf4j.LoggerFactory

class StripeGateway : PaymentGateway {
    private val log = LoggerFactory.getLogger(StripeGateway::class.java)

    override fun pay(amount: Int): PaymentStatus {
        log.info("Paying {} in Stripe", amount)
        return if (amount % 2 == 0) PaymentStatus.SUCCESS else PaymentStatus.FAILURE
    }
}