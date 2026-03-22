# Incident Response Procedures

## Severity Levels

| Level | Response Time | Examples |
|-------|-------------|----------|
| Critical | Immediate | Financial calc divergence, data corruption, equivalence test failure |
| Warning | Within 1 hour | High token spend, coverage drop, agent idle |
| Info | Next business day | Minor metric drift, non-blocking warnings |

## Critical: Financial Calculation Divergence

1. **Immediately** pause all Polecat agents working on the affected module
2. **Rollback** the deployed service to the previous version
3. **Investigate**: Compare COBOL output with Java output for the divergent case
4. **Root cause**: Check BigDecimal usage, rounding modes, arithmetic operations
5. **Fix**: Correct the translation, add the divergent case as an equivalence test
6. **Validate**: Re-run full equivalence test suite
7. **Re-deploy**: Only after Witness agent approves

## Critical: Equivalence Test Failure

1. **Block** the merge in the Refinery queue
2. **Identify** which test cases failed
3. **Compare** expected vs actual output field by field
4. **Classify**: Is this a real bug or an intentional improvement?
   - Bug: fix and re-test
   - Improvement: document justification, get human approval
5. **Re-validate**: Full test suite must pass before unblocking merge

## Warning: High Token Spend

1. **Identify** which agent/task is consuming the most tokens
2. **Check** for agent loops (same analysis repeated)
3. **Optimize**: Break large modules into smaller translation units
4. **Cache**: Verify common patterns are being reused, not re-analyzed
5. **Adjust**: Set per-task token limits if needed

## Warning: Agent Idle

1. **Check** if the agent's task completed (success or failure)
2. **Check** if the agent is waiting on a dependency (blocked by another agent)
3. **Restart** if the agent appears stuck
4. **Reassign** the task if the agent cannot recover
5. **Log** the incident for pattern analysis
