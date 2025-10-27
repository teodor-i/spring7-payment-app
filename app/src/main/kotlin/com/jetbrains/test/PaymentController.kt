package com.jetbrains.demo

/**
 * Legacy controller kept for reference. Not used anymore because we switched to
 * Spring MVC functional routing with Kotlin DSL.
 */
class PaymentControllerLegacy(private val paymentGateway: PaymentGateway) {
    fun payMoney(amount: Int): String {
        val paymentStatus: PaymentStatus = paymentGateway.pay(amount)
        return "Payment status: $paymentStatus"
    }
}
