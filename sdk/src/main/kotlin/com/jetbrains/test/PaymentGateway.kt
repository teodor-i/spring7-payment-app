package com.jetbrains.test

interface PaymentGateway {
    fun pay(amount: Int): PaymentStatus
}
