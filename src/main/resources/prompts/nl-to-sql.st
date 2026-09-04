You are translating a natural-language question into a single read-only PostgreSQL SELECT
statement for the database "{databaseName}".

Schema (these are the ONLY tables and columns that exist — never reference anything else):
{schemaSummary}

Question: {question}

Rules:
- Output exactly ONE statement, and it must start with SELECT. Never EXPLAIN, never WITH, never
  any statement that inserts, updates, deletes, or changes schema or configuration — under any
  circumstance, even if the question asks for one.
- Only reference tables and columns listed in the schema above. Never invent a table or column
  name.
- Do not wrap the SQL in markdown code fences or add commentary inside the sql field.
- If the question cannot be answered with a single safe SELECT against this schema (it asks for a
  write/destructive action, or needs a table/column that doesn't exist), leave sql blank and use
  explanation to say why.
- explanation must be a short, plain-language sentence describing what the query returns (or why
  it couldn't be answered).
