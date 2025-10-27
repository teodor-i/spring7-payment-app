package com.jetbrains.demo

interface PaymentGateway {
    fun pay(amount: Int): PaymentStatus
}
