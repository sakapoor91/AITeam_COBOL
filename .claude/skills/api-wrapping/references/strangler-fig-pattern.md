# Strangler Fig Pattern for Legacy Migration

## Overview
The strangler fig pattern gradually replaces legacy system functionality with new services. An API gateway routes requests to either the legacy COBOL system or the new Java service based on migration progress.

## Implementation Stages

### Stage 1: Proxy (Read-Only)
```
Client → API Gateway → Legacy COBOL System
                    ↘ New Java Service (shadow mode, read-only)
```
- New service receives a copy of requests but responses come from COBOL
- Compare outputs for equivalence testing
- Zero risk: legacy system is still authoritative

### Stage 2: Parallel Run
```
Client → API Gateway → Legacy COBOL System (primary)
                    → New Java Service (secondary)
                    ← Compare responses, alert on divergence
```
- Both systems process requests
- Legacy responses are returned to client
- Java responses are logged and compared

### Stage 3: Canary Switch
```
Client → API Gateway → New Java Service (90%)
                    → Legacy COBOL System (10% canary)
```
- Gradually shift traffic to new service
- Keep legacy running for comparison and fallback
- Monitor equivalence rate and error rates

### Stage 4: Full Cutover
```
Client → API Gateway → New Java Service (100%)
                    → Legacy COBOL System (decommission after soak period)
```
- All traffic to new service
- Legacy system kept running for 30-day soak period
- Decommission after soak period with no issues

## API Gateway Configuration
```yaml
routes:
  - path: /api/v1/accounts/{id}
    module: COACTUPC
    strategy: canary
    new_service: http://java-service:8080
    legacy_service: http://cics-gateway:3270
    canary_percentage: 10
```

## Rollback Procedure
1. Set canary_percentage to 0
2. All traffic returns to legacy system immediately
3. Investigate Java service issues
4. Fix, re-validate, and gradually increase canary again
