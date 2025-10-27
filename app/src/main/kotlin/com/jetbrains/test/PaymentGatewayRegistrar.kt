package com.jetbrains.demo

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanRegistrar
import org.springframework.beans.factory.BeanRegistry
import org.springframework.core.env.Environment

/**
 * Dynamically registers PaymentGateway beans based on configuration properties.
 */
class PaymentGatewayRegistrar : BeanRegistrar {
    private val log = LoggerFactory.getLogger(PaymentGatewayRegistrar::class.java)

    private companion object {
        const val MULTI_PROP = "app.payment.gateways.classes"
        const val SINGLE_PROP = "app.payment.gateway.class"
        const val PRIMARY_BEAN = "gateway"
    }

    override fun register(
        registry: BeanRegistry,
        env: Environment
    ) {
        val resolved = resolveGatewayClasses(env)
        resolved.forEachIndexed { index, clazz ->
            val name = if (index == 0) PRIMARY_BEAN else "$PRIMARY_BEAN${index + 1}"
            registry.registerBean(name, clazz)
            log.info("Registered PaymentGateway bean '{}' -> {}", name, clazz.name)
        }
    }

    private fun resolveGatewayClasses(env: Environment): List<Class<*>> {
        val multi = env.getProperty(MULTI_PROP)
            ?.split(',')
            ?.asSequence()
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.mapNotNull { fqcn ->
                try {
                    Class.forName(fqcn)
                } catch (_: ClassNotFoundException) {
                    log.warn("Skipping provider '{}': class not found", fqcn)
                    null
                }
            }
            ?.toList()
            ?: emptyList()

        if (multi.isNotEmpty()) return multi

        // Backward compatibility: single class property
        val single = env.getProperty(SINGLE_PROP)
        val clazz = if (!single.isNullOrBlank()) {
            try {
                Class.forName(single)
            } catch (e: ClassNotFoundException) {
                // Preserve previous behavior: fail fast for invalid single provider
                throw RuntimeException(e)
            }
        } else NullGateway::class.java

        return listOf(clazz)
    }
}