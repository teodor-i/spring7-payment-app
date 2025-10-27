# Spring 7 Payment Gateway — Dynamic Bean Registration + Kotlin Routing DSL

A sample **Spring 7 / Kotlin** application demonstrating:
- **Dynamic bean registration** using a custom *Bean Registrar DSL*
- **Declarative, annotation-free HTTP routing** via the *Kotlin Routing DSL*

This project shows how to build a modular **Payments Gateway** where each provider (Stripe, PayPal, ApplePay, etc.) can be added, removed, or configured **without changing central controllers or configuration classes**.

---

### Key Features

### Dynamic Provider Registration
- Registers any number of `PaymentGateway` beans at startup based on configuration.
- Gracefully skips invalid or missing providers and falls back to a safe default (`NullGateway`).
- Supports drop-in JAR modules — new providers become available with a single property update.

### Kotlin Routing DSL
- Functional, concise definition of REST endpoints (`/api/pay`, `/api/payments/providers`, `/api/health`).
- Built-in request-timing filter for lightweight logging.
- Handles `HEAD`, `OPTIONS`, and path-variable routes without extra annotations.

---

## Configuration Examples

```properties
# Single provider (backward compatible)
app.payment.gateway.class=com.jetbrains.test.payment.StripeGateway

# Multiple providers
app.payment.gateways.classes=com.jetbrains.test.payment.StripeGateway,com.jetbrains.test.payment.PaypalGateway
