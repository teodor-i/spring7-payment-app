package com.jetbrains.test

import org.slf4j.LoggerFactory

class NullGateway : PaymentGateway {
    private val log = LoggerFactory.getLogger(NullGateway::class.java)

    override fun pay(amount: Int): PaymentStatus {
        log.info("Paying {} in NullGateway", amount)
        return PaymentStatus.SUCCESS
    }
}