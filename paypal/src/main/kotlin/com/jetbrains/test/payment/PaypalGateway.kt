package com.jetbrains.demo.payment

import com.jetbrains.demo.PaymentGateway
import com.jetbrains.demo.PaymentStatus
import org.slf4j.LoggerFactory

class PaypalGateway : PaymentGateway {
    private val log = LoggerFactory.getLogger(PaypalGateway::class.java)

    override fun pay(amount: Int): PaymentStatus {
        log.info("Paying {} in Paypal", amount)
        return if (amount > 100) PaymentStatus.FAILURE else PaymentStatus.SUCCESS
    }
}