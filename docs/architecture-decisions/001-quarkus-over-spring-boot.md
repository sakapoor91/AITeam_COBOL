# ADR-001: Quarkus Over Spring Boot

## Status

Accepted

## Context

The target platform for COBOL-to-Java translation requires a modern Java framework for building REST APIs and microservices. The two leading candidates are Spring Boot and Quarkus.

Key considerations:

- Generated services will run in containerized environments (Docker/Kubernetes)
- Banking workloads require predictable latency and fast startup for scaling events
- The project generates many small, focused microservices (one per COBOL module)
- Developer familiarity with Spring Boot is higher across the industry, but generated code reduces the importance of manual developer productivity
- The project explicitly avoids reactive/WebFlux patterns in favor of the servlet model for banking reliability

## Decision

We will use **Quarkus** as the primary Java framework for all generated services. Spring Boot remains acceptable as a fallback when specific integrations require it, but Quarkus is the default.

Key factors in this decision:

- **Startup time**: Quarkus starts in sub-second times, compared to 5-15 seconds for equivalent Spring Boot applications. For a platform generating dozens of microservices, this compounds into significant operational advantage.
- **Memory footprint**: Quarkus applications consume 50-70% less memory at runtime. With many small services per module, memory efficiency directly reduces infrastructure cost.
- **Native compilation**: GraalVM native image support enables ahead-of-time compilation, further reducing startup time and memory for production deployments.
- **CDI over Spring DI**: Quarkus uses build-time CDI (Contexts and Dependency Injection), which catches dependency injection errors at compile time rather than runtime -- an important safety property for generated code.
- **Imperative model**: Quarkus RESTEasy (JAX-RS) supports the imperative servlet model required by the project's banking reliability constraints, without defaulting to reactive patterns.

## Consequences

### Positive

- Faster container startup enables rapid horizontal scaling during peak transaction periods
- Lower memory consumption reduces cloud infrastructure costs across the full service fleet
- Build-time CDI validation catches configuration errors before deployment
- Native image compilation available for latency-sensitive services

### Negative

- Smaller ecosystem of third-party libraries compared to Spring Boot; some banking-specific libraries may require adaptation
- Fewer developers have Quarkus experience, which increases onboarding time for teams maintaining the generated code
- Some Spring-specific patterns in reference architectures (like Apache Fineract) require translation to Quarkus equivalents

### Neutral

- Since code is AI-generated, framework-specific boilerplate differences between Quarkus and Spring Boot have minimal impact on translation agent complexity
