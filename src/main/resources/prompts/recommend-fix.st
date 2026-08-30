You are analyzing a slow SQL query captured from a PostgreSQL database named "{databaseName}".

Query (normalized, literal values stripped):
{normalizedQuery}

Observed performance:
- calls: {calls}
- mean execution time: {meanExecTimeMs} ms
- total execution time: {totalExecTimeMs} ms

Diagnose the most likely root cause (missing index, sequential scan, lock contention, or a
misconfigured instance parameter) and propose exactly ONE safe, minimal remediation.

Rules for the proposed fix:
- type must be one of INDEX, CONFIG_CHANGE, or QUERY_REWRITE.
- For INDEX, proposedSql must be a single "CREATE INDEX CONCURRENTLY ..." statement.
- For CONFIG_CHANGE, proposedSql must be a single "ALTER SYSTEM SET ..." statement.
- For QUERY_REWRITE, proposedSql should contain the rewritten query as advisory text only - it
  will not be executed automatically.
- proposedSql must NEVER be empty or blank. If the query has no table, column, or config
  parameter that could realistically be optimized (for example, a call to a function like
  pg_sleep() with no underlying schema object), use type QUERY_REWRITE and put a short, concrete
  explanation of that fact directly in proposedSql (for example: "No schema or configuration
  change applies - this call has no underlying table or index to optimize; investigate why the
  application issues it.").
- Never propose more than one statement, and never DROP or ALTER TABLE.
- explanation must be written in plain language for a DBA who has not seen this query before.
- targetObject is the table, index, or config parameter name the fix affects.
- aiAssessedRisk must be one of LOW, MEDIUM, or HIGH.
