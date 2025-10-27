Kotlin Spring Routing DSL is already used by ~10% of users and will possibly grow further, as it complements the new Spring Bean Registry DSL upcoming in Spring 7.0.

From the Broadcom perspective, Sebastién Deleuze is also promoting projects like PetKlinik, which uses Routing DSL. Lacking support of it could be a problem.

Scenario
We plan to show a scenario during our Spring 7.0 release event stream.
Show that with Spring 7.0’s new BeanRegistrar DSL, developers can easily register beans dynamically, and that it pairs naturally with Kotlin’s routing DSL. Highlight that this makes apps more modular, pluggable, and configurable and also show advantage of this "functional" approach.
Checked with Seb - looks like a good demonstration.

When Dynamic bean registration could be beneficial:

Providers vary per deployment or customer

Example: one SaaS customer wants PayPal + Stripe, another only Stripe, and an enterprise customer adds their own in-house provider.

→ You don’t know at build time which providers will actually be needed, because it depends on customer configuration.

Runtime configuration and feature flags

Example: enable ApplePay only if an API key is present in application.yml; disable Klarna in dev profile.

→ The set of beans adapts to environment, DB entries, or feature flags.

Open-ended / extensible provider set

Example: you ship your app with a plugin mechanism - new payment modules can be dropped in as JARs at any time.

→ The number and identity of providers isn’t fixed; the registrar loops over what’s available and registers them dynamically.

A/B testing and gradual rollouts

Example: new feature A/B test; or roll out a new fraud-detection provider only for beta users.

→ Registrar makes it easy to express conditional bean creation and orchestration logic in code instead of tangled annotations.

Use case

We’re building a Payments Gateway that serves many client shops/merchants.

Each merchant enables a different subset of providers: Stripe, PayPal, ApplePay, Klarna, etc.
Every provider implements PaymentGateway (pay, refund, healthCheck) and ships as a separate module/JAR.
Configuration (classpath + application.yml/properties, feature flags) selects which providers are active per environment and per tenant.
A central routing module (A/B%, risk score, fallback chain) chooses a provider per transaction.
Without BeanRegistrar DSL

Mention those:

Developers would need one @Bean method per provider. Each method would require @ConditionalOnProperty or profile annotations to control activation.
The result is repetitive, and hard to maintain when providers grow in number.
Adding a new provider means touching central config classes, risking merge conflicts, and scattering business logic across annotations.
Feature-flagging or A/B logic (e.g., "20% of VISA transactions to Stripe") is painful to make declaratively.
With BeanRegistrar DSL

Loop programmatically over config/tenants and register only enabled providers (named beans, clear logs)
Adding a new provider is as simple as dropping in a new JAR and one line in config.
Configure complex rules (A/B testing, risk scoring, feature flags) easier in code.
Kotlin Routing DSL
Kotlin Routing DSL advantages for Spring 7.0 use case:

1. Routing DSL: Faster: throughput, latency around 10%+ boost. There is less processing in terms of content type resolution, defining which type of converter you are going to use to serialize code.

2. Flexibility: Like having For-loops and if-statements for(in) endpoints, when people need something more flexible - you can’t just do it with regular annotation-based approach. (IDEA couldn’t show that though).

Dynamic endpoints. Nested endpoints.

3. More idiomatic way

You can split the routes. With your DSL you can write your own Gets Puts with method references. And your request and respond in another class. With annotations - you need to write it right after annotations. For big projects, that could be a problem.

4. Kotlin specific: Readability, DSL support

Demo storyline
Goal: Show how Spring 7.0 Bean Registrar + Kotlin Routing DSL give you plug-and-play providers, smart orchestration, and zero controller changes.

Start simple: GET /api/payments/providers shows Stripe + PayPal enabled.
Config switch: in application.properties, disable Stripe → restart → only PayPal remains.
Drop-in JAR: add ApplePay module + one line in config → instantly available.
Dev mode fallback: missing API key → Bean Registrar auto-skips provider with a log message.
Smart orchestration: show A/B test rule (20% VISA → Stripe, rest PayPal or roll-out of a new paying system), risk detection sending high-risk orders to PayPal.
Routing DSL highlight:
Concise coRouter block for /payments.
Show filter logging execution time.
Emphasize how DSLs are Kotlin strength: both bean registration and routing look clean, functional, and expressive.
GitHub Project (in progress)


Configuration notes (added)
- Single provider (backward compatible): set
  - app.payment.gateway.class=com.jetbrains.test.payment.StripeGateway
  - or switch to PayPal with app.payment.gateway.class=com.jetbrains.test.payment.PaypalGateway
- Multiple providers: enable both with
  - app.payment.gateways.classes=com.jetbrains.test.payment.StripeGateway,com.jetbrains.test.payment.PaypalGateway
  - Unknown classes are skipped with a log message; in dev, if none are valid, NullGateway is registered.

Routing DSL highlight (implemented)
- Functional MVC routes defined via RouterFunctions.route() in RoutesConfig.
- Request timing filter logs: "METHOD PATH -> STATUS (X ms)" for every request.

---

Runnable scenario: demonstrating Bean Registrar possibilities (Spring 7 BeanRegistrar + Kotlin Routing DSL)

This scenario is crafted to be copy/paste friendly for a live demo or a recording. It shows single provider, multi-provider, invalid class fallback, and how adding a new JAR makes a provider available without touching controllers or configs beyond one property line.

Prerequisites
- JDK 21+
- Maven 3.9+

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

New/updated endpoints
- POST /api/pay — JSON {"amount": Int} or text/plain number; returns {"status", "amount"}
- GET  /api/pay/{amount} — path variable variant; returns {"status", "amount"}
- HEAD /api/pay — returns headers (X-Endpoints) only
- OPTIONS /api/pay — returns Allow header with supported methods
- GET  /api/payments/providers — returns all active provider class names
- GET  /api/health — simple health probe with providersCount and timestamp
