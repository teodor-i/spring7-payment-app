package com.jetbrains.demo

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.RouterFunctions

@Configuration
class RoutesConfig {

    private val log = LoggerFactory.getLogger(RoutesConfig::class.java)

    @Bean
    fun httpApiRoutes(handler: PaymentHandler): RouterFunction<ServerResponse> =
        RouterFunctions.route()
            .filter { req, next ->
                val start = System.nanoTime()
                val resp = next.handle(req)
                val tookMs = (System.nanoTime() - start) / 1_000_000
                log.info("{} {} -> {} ({} ms)", req.method(), req.path(), resp.statusCode(), tookMs)
                resp
            }
            // Helpful index to avoid confusion about base URL/port
            .GET("/") { _ -> ServerResponse.ok().body(mapOf(
                "message" to "Spring 7 Payment App is running on port 8080",
                "try" to listOf(
                    "GET /api/payments/providers",
                    "POST /api/pay with {\"amount\":42}",
                    "GET /api/health"
                )
            )) }
            .GET("/api") { _ -> ServerResponse.ok().body(mapOf(
                "endpoints" to listOf(
                    "GET /api/pay/{amount}",
                    "POST /api/pay",
                    "HEAD /api/pay",
                    "OPTIONS /api/pay",
                    "GET /api/payments/providers",
                    "GET /api/health"
                )
            )) }
            // Payments domain
            .GET("/api/pay/{amount}") { req -> handler.payPath(req) }
            .POST("/api/pay") { req -> handler.pay(req) }
            .HEAD("/api/pay") { req -> handler.headPay(req) }
            .OPTIONS("/api/pay") { req -> handler.optionsPay(req) }
            // Discovery and health (support both with and without trailing slash)
            .GET("/api/payments/providers") { req -> handler.providers(req) }
            .GET("/api/payments/providers/") { req -> handler.providers(req) }
            .GET("/api/health") { req -> handler.health(req) }
            .build()
}
