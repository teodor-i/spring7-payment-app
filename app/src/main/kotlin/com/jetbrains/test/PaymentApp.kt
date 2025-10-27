package com.jetbrains.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(PaymentGatewayRegistrar::class)
class PaymentApp

fun main(args: Array<String>) {
    runApplication<PaymentApp>(*args)
}
