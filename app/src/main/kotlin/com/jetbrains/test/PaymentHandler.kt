package com.jetbrains.demo

import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.body
import org.springframework.context.ApplicationContext

@Component
class PaymentHandler(
    @org.springframework.beans.factory.annotation.Qualifier("gateway")
    private val paymentGateway: PaymentGateway,
    private val applicationContext: ApplicationContext
) {
    data class PayRequest(val amount: Int)

    fun pay(request: ServerRequest): ServerResponse {
        // Accept either JSON {"amount": 123} or raw number body
        val amount = runCatching { request.body<PayRequest>().amount }
            .getOrElse {
                // try reading as plain integer
                runCatching { request.body<Int>() }.getOrElse { 0 }
            }
        val status: PaymentStatus = paymentGateway.pay(amount)
        return ServerResponse.ok().body(mapOf("status" to status.name, "amount" to amount))
    }

    fun payPath(request: ServerRequest): ServerResponse {
        val amountStr = request.pathVariable("amount")
        val amount = amountStr.toIntOrNull() ?: 0
        val status: PaymentStatus = paymentGateway.pay(amount)
        return ServerResponse.ok().body(mapOf("status" to status.name, "amount" to amount))
    }

    fun headPay(_request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
            .header("X-Endpoints", "GET /api/pay/{amount}, POST /api/pay, HEAD /api/pay, OPTIONS /api/pay")
            .build()
    }

    fun optionsPay(_request: ServerRequest): ServerResponse {
        return ServerResponse.ok()
            .header("Allow", "GET,POST,HEAD,OPTIONS")
            .build()
    }

    fun providers(_request: ServerRequest): ServerResponse {
        // Show all active PaymentGateway beans (could be multiple)
        val beans = applicationContext.getBeansOfType(PaymentGateway::class.java)
        val providerClasses = beans.values.map { it.javaClass.name }.sorted()
        return ServerResponse.ok().body(mapOf("providers" to providerClasses))
    }

    fun health(_request: ServerRequest): ServerResponse {
        val beans = applicationContext.getBeansOfType(PaymentGateway::class.java)
        return ServerResponse.ok().body(mapOf(
            "status" to "UP",
            "providersCount" to beans.size,
            "timestamp" to System.currentTimeMillis()
        ))
    }
}
