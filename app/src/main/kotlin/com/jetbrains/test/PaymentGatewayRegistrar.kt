package com.jetbrains.test

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanRegistrar
import org.springframework.beans.factory.BeanRegistry
import org.springframework.core.env.Environment

class PaymentGatewayRegistrar: BeanRegistrar {
    private val log = LoggerFactory.getLogger(PaymentGatewayRegistrar::class.java)

    override fun register(
        registry: BeanRegistry,
        env: Environment
    ) {
        // Try multi-provider property first: comma-separated list of FQCNs
        val multiProp = env.getProperty("app.payment.gateways.classes")
        val classes: List<Class<*>> = if (!multiProp.isNullOrBlank()) {
            multiProp.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { fqcn ->
                    try {
                        Class.forName(fqcn)
                    } catch (e: ClassNotFoundException) {
                        log.warn("Skipping provider '{}': class not found", fqcn)
                        null
                    }
                }
                .ifEmpty {
                    log.warn("No valid classes from app.payment.gateways.classes; falling back to single property or NullGateway")
                    emptyList()
                }
        } else {
            emptyList()
        }

        val resolved = if (classes.isNotEmpty()) {
            classes
        } else {
            // Backward compatibility: single class property
            val single = env.getProperty("app.payment.gateway.class")
            val clazz = if (!single.isNullOrBlank()) {
                try {
                    Class.forName(single)
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException(e)
                }
            } else NullGateway::class.java
            listOf(clazz)
        }

        resolved.forEachIndexed { index, clazz ->
            val name = if (index == 0) "gateway" else "gateway${index + 1}"
            registry.registerBean(name, clazz)
            log.info("Registered PaymentGateway bean '{}' -> {}", name, clazz.name)
        }
    }
}