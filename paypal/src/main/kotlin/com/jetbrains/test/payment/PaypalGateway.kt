package com.jetbrains.test.payment

import com.jetbrains.test.PaymentGateway
import com.jetbrains.test.PaymentStatus
import org.slf4j.LoggerFactory

class PaypalGateway : PaymentGateway {
    private val log = LoggerFactory.getLogger(PaypalGateway::class.java)

    override fun pay(amount: Int): PaymentStatus {
        log.info("Paying {} in Paypal", amount)
        return if (amount > 100) PaymentStatus.FAILURE else PaymentStatus.SUCCESS
    }
}