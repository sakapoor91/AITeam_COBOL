# ADR-004: PostgreSQL Over Oracle for Persistence

## Status

Accepted

## Context

The modernized Java services require a relational database for persistence, replacing COBOL VSAM file storage. The primary candidates are Oracle Database (the banking industry incumbent) and PostgreSQL.

Key considerations:

- The project targets Apache Fineract compatibility, which supports PostgreSQL natively
- Many legacy COBOL modernization projects introduce Oracle dependencies that create long-term vendor lock-in and significant licensing costs
- The target architecture emphasizes open-source components to minimize total cost of ownership
- PostgreSQL must support the same data integrity guarantees that banking workloads require: ACID transactions, row-level locking, and reliable replication

## Decision

We will use **PostgreSQL** as the sole relational database for all generated services. No Oracle dependencies will be introduced at any layer of the stack.

Specific guidelines:

- Use PostgreSQL `NUMERIC` / `DECIMAL` types for monetary columns (maps from COBOL COMP-3)
- Use standard SQL and JPA/Hibernate for data access to avoid PostgreSQL-specific lock-in where possible
- Use PostgreSQL `JSONB` sparingly for semi-structured data that does not require relational integrity
- Deploy PostgreSQL via container (Docker) for development and managed service (e.g., AWS RDS, Azure Database for PostgreSQL) for production

## Consequences

### Positive

- Zero licensing cost eliminates a major expense line in modernization budgets
- Native Fineract compatibility without adapter layers
- Strong open-source ecosystem with extensive tooling, monitoring, and community support
- ACID compliance, row-level locking, and mature replication meet banking transaction requirements
- Avoids vendor lock-in that would constrain future infrastructure decisions

### Negative

- Some banking teams have deep Oracle expertise and established operational procedures that do not transfer directly to PostgreSQL
- Certain Oracle-specific features (Real Application Clusters, Advanced Queuing) have no direct PostgreSQL equivalent, though alternatives exist
- Migration of existing Oracle-based systems to PostgreSQL adds scope if the organization has Oracle infrastructure in place

### Neutral

- PostgreSQL performance is comparable to Oracle for the transaction volumes typical in module-level banking services; neither database is a bottleneck at this scale
