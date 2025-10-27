# Spring 7 Payment Gateway — Dynamic Bean Registration + Kotlin Routing DSL

A sample **Spring 7 / Kotlin** application demonstrating:
- **Dynamic bean registration** using a custom *Bean Registrar DSL*
- **Declarative, annotation-free HTTP routing** via the *Kotlin Routing DSL*

This project shows how to build a modular **Payments Gateway** where each provider (Stripe, PayPal, ApplePay, etc.) can be added, removed, or configured **without changing central controllers or configuration classes**.


### Key Features

### Dynamic Provider Registration
- Registers any number of `PaymentGateway` beans at startup based on configuration.
- Gracefully skips invalid or missing providers and falls back to a safe default (`NullGateway`).
- Supports drop-in JAR modules — new providers become available with a single property update.

### Kotlin Routing DSL
- Functional, concise definition of REST endpoints (`/api/pay`, `/api/payments/providers`, `/api/health`).
- Built-in request-timing filter for lightweight logging.
- Handles `HEAD`, `OPTIONS`, and path-variable routes without extra annotations.


## Configuration Examples

```properties
# Single provider (backward compatible)
app.payment.gateway.class=com.jetbrains.test.payment.StripeGateway

# Multiple providers
app.payment.gateways.classes=com.jetbrains.test.payment.StripeGateway,com.jetbrains.test.payment.PaypalGateway


Build everything
- mvn -q -DskipTests package

Step 1 — Start with a single provider (Stripe)
- Run: mvn -q -pl app spring-boot:run -Dspring-boot.run.profiles=stripe
- Verify providers:
  - curl -s http://localhost:8080/api/payments/providers
  - Expected: {"providers":["com.jetbrains.test.payment.StripeGateway"]}
- Make a payment (JSON):
  - curl -s -X POST http://localhost:8080/api/pay -H 'Content-Type: application/json' -d '{"amount": 42}'
- Make a payment (path variable):
  - curl -s http://localhost:8080/api/pay/42
- Inspect timing logs from the Routing DSL filter in the app console.

Step 2 — Switch to another single provider (PayPal)
- Stop the app and run with the paypal profile:
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.profiles=paypal
- Verify providers:
  - curl -s http://localhost:8080/api/payments/providers
  - Expected: {"providers":["com.jetbrains.test.payment.PaypalGateway"]}

Step 3 — Enable multiple providers at once (Stripe + PayPal)
- Stop the app, then run with both providers using the multi-property:
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.arguments="--app.payment.gateways.classes=com.jetbrains.test.payment.StripeGateway,com.jetbrains.test.payment.PaypalGateway"
- Verify providers (order may vary):
  - curl -s http://localhost:8080/api/payments/providers
  - Expected contains StripeGateway and PaypalGateway
- Show that no controller changes were required; the Bean Registrar registered gateway, gateway2 automatically.

Step 4 — Invalid/missing class fallback
- Run with an invalid class name plus a valid one:
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.arguments="--app.payment.gateways.classes=com.acme.DoesNotExist,com.jetbrains.test.payment.StripeGateway"
- Observe log: Skipping provider 'com.acme.DoesNotExist': class not found
- Verify providers returns only the valid one.
- Run with only invalid classes:
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.arguments="--app.payment.gateways.classes=com.acme.Missing,com.acme.AlsoMissing"
- Observe log: No valid classes ... falling back to single property or NullGateway
- Verify providers contains com.jetbrains.test.NullGateway

Step 5 — Drop-in provider JAR (conceptual)
- Package a new module that implements com.jetbrains.test.PaymentGateway (e.g., ApplePayGateway in applepay.jar) and place it on the classpath (add as a Maven module or a dependency).
- Start the app with:
  - --app.payment.gateways.classes=com.jetbrains.test.payment.StripeGateway,com.example.applepay.ApplePayGateway
- Verify providers now includes ApplePayGateway — no central configuration class changes required.

Step 6 — Health and HTTP method variety (Routing DSL showcase)
- Health endpoint:
  - curl -s http://localhost:8080/api/health
- HEAD and OPTIONS for /api/pay:
  - curl -i -X HEAD http://localhost:8080/api/pay
  - curl -i -X OPTIONS http://localhost:8080/api/pay
- Path-variable variant (no body):
  - curl -s http://localhost:8080/api/pay/7

What this scenario proves
- Bean Registrar DSL lets you:
  - Register a variable number of provider beans from configuration at startup.
  - Skip unknown providers gracefully and fall back to a safe default (NullGateway) when needed.
  - Add providers by dropping in a new JAR and a single property update — no controller or central @Configuration changes.
- Kotlin Routing DSL lets you:
  - Keep endpoints concise and flexible; show HEAD/OPTIONS handling, path variables, and a lightweight logging filter without annotations.

Reference — configuration properties
- Single provider (backward compatible):
  - app.payment.gateway.class=com.jetbrains.test.payment.StripeGateway
- Multiple providers:
  - app.payment.gateways.classes=com.jetbrains.test.payment.StripeGateway,com.jetbrains.test.payment.PaypalGateway
- Behavior:
  - Unknown classes are logged and skipped.
  - If all classes are invalid, the registrar registers NullGateway.
