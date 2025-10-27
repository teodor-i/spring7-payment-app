# Spring 7 Payment Gateway - Dynamic Bean Registration + Kotlin Routing DSL

A sample **Spring 7 / Kotlin** application demonstrating:
- **Dynamic bean registration** using a custom *Bean Registrar DSL*
- **Declarative, annotation-free HTTP routing** via the *Kotlin Routing DSL*

This project shows how to build a modular **Payments Gateway** where each provider (Stripe, PayPal, ApplePay, etc.) can be added, removed, or configured **without changing central controllers or configuration classes**.

### Use case

We’re building a Payments Gateway that serves many client shops/merchants.

- Each merchant enables a different subset of providers: Stripe, PayPal, ApplePay, Klarna, etc. 
- Every provider implements PaymentGateway (pay, refund, healthCheck) and ships as a separate module/JAR. 
- Configuration (classpath + application.yml/properties, feature flags) selects which providers are active per environment and per tenant. 
- A central routing module, which can be used for A/B%, risk score, fallback chain, chooses a provider per transaction.

### Key Features

### Dynamic Provider Registration
- Registers any number of `PaymentGateway` beans at startup based on configuration.
- Gracefully skips invalid or missing providers and falls back to a safe default (`NullGateway`).
- Supports drop-in JAR modules - new providers become available with a single property update.

### Kotlin Routing DSL
- Functional, concise definition of REST endpoints (`/api/pay`, `/api/payments/providers`, `/api/health`).
- Built-in request-timing filter for lightweight logging.
- Handles `HEAD`, `OPTIONS`, and path-variable routes without extra annotations.

## Instructions
Build everything
- mvn -q -DskipTests package

Step 1 - Start with a single provider (Stripe)
- Run: mvn -q -pl app spring-boot:run -Dspring-boot.run.profiles=stripe
- Verify providers:
  - curl -s http://localhost:8080/api/payments/providers
  - Expected: {"providers":["com.jetbrains.demo.payment.StripeGateway"]}
- Make a payment (JSON):
  - curl -s -X POST http://localhost:8080/api/pay -H 'Content-Type: application/json' -d '{"amount": 42}'
- Make a payment (path variable):
  - curl -s http://localhost:8080/api/pay/42
- Inspect timing logs from the Routing DSL filter in the app console.

Step 2 - Switch to another single provider (PayPal)
- Stop the app and run with the paypal profile:
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.profiles=paypal
- Verify providers:
  - curl -s http://localhost:8080/api/payments/providers
  - Expected: {"providers":["com.jetbrains.demo.payment.PaypalGateway"]} 
- Make a payment (JSON):
  - curl -s -X POST http://localhost:8080/api/pay -H 'Content-Type: application/json' -d '{"amount": 42}'

Step 3 - Enable multiple providers at once (Stripe + PayPal)
- Stop the app, then run with both providers using the multi-property:
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.arguments="--app.payment.gateways.classes=com.jetbrains.demo.payment.StripeGateway,com.jetbrains.demo.payment.PaypalGateway"
- Verify providers (order may vary):
  - curl -s http://localhost:8080/api/payments/providers
  - Expected contains StripeGateway and PaypalGateway
- Show that no controller changes were required; the Bean Registrar registered gateway, gateway2 automatically.

Step 4 - Invalid/missing class fallback
- Run with an invalid class name plus a valid one:
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.arguments="--app.payment.gateways.classes=com.acme.DoesNotExist,com.jetbrains.demo.payment.StripeGateway"
- Observe log: Skipping provider 'com.acme.DoesNotExist': class not found
- Verify providers returns only the valid one.
- Run with only invalid classes:
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.arguments="--app.payment.gateways.classes=com.acme.Missing,com.acme.AlsoMissing"
- Observe log: No valid classes ... falling back to single property or NullGateway
- Verify providers contains com.jetbrains.demo.NullGateway

Step 5 - Drop-in provider JAR (conceptual)
- Package/build first:
  - If you added a new module (e.g., applepay): mvn -q -DskipTests -pl applepay clean package
  - If you added a third‑party JAR: declare it as a dependency in app/pom.xml, then from project root run: mvn -q -DskipTests clean install
- Start the app with the property (full command):
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.arguments="--app.payment.gateways.classes=com.jetbrains.demo.payment.StripeGateway,com.example.applepay.ApplePayGateway"
- Verify providers now includes ApplePayGateway - no central configuration class changes required.

Step 6 - Health and HTTP method variety (Routing DSL showcase)
- Start the app (any profile; example: Stripe):
  - mvn -q -pl app spring-boot:run -Dspring-boot.run.profiles=stripe
- Health endpoint:
  - curl -s http://localhost:8080/api/health
- HEAD and OPTIONS for /api/pay:
  - curl -i -X HEAD http://localhost:8080/api/pay
  - curl -i -X OPTIONS http://localhost:8080/api/pay
- Path-variable variant (no body):
  - curl -s http://localhost:8080/api/pay/7
    - Note: With the Stripe profile, odd amounts return FAILURE by design (demo logic in StripeGateway).
  - curl -s http://localhost:8080/api/pay/42
    - Example of a SUCCESS response with Stripe (even amounts => SUCCESS).

What this scenario proves
- Bean Registrar DSL lets you:
  - Register a variable number of provider beans from configuration at startup.
  - Skip unknown providers gracefully and fall back to a safe default (NullGateway) when needed.
  - Add providers by dropping in a new JAR and a single property update - no controller or central @Configuration changes.
- Kotlin Routing DSL lets you:
  - Keep endpoints concise and flexible; show HEAD/OPTIONS handling, path variables, and a lightweight logging filter without annotations.

### Routing DSL advantages
1. **A performance boost:** Routing with the DSL usually shaves off some overhead-around 5–10% in many setups. The reason? It doesn’t need to scan annotations or resolve converters at runtime the same way annotation-based controllers do. For most apps, that’s not a game-changer, but at scale it’s free performance. 
2. **Flexibility**: Ever wanted to register routes in a loop based on config? Or enable an endpoint only under certain conditions? With annotations, you’re stuck-routes are static. With the DSL, you can use if, for, or when just like in any Kotlin code. You also get nesting, which makes APIs easier to structure.
3. **Logic separation possibility**: With the Routing DSL, you can keep route definitions separate from handler implementations-for example, by centralizing routes in one file and delegating to handler functions or classes (handler::handleLogin). Annotation-based controllers can also be modular, but the DSL goes further by allowing programmatic composition: routes can be grouped, combined, or reused just like normal Kotlin code. 
4. **Readability**: Kotlin DSL is concise, expressive, and leverages the language’s DSL capabilities. Instead of sprinkling Java-style annotations everywhere, you write routes in a way that reads like a Kotlin-native API. It’s more readable.
