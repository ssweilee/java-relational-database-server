A simplified **relational database server implemented in Java**.  
This project showcases my ability to design and implement core database functionality from scratch, including query parsing, persistent storage, and data relationship management.

## Features

- **Persistent storage:** All data stored as `.tab` files in the `databases/` folder.  
- **SQL query language support:**  
  - `USE`, `CREATE`, `INSERT`, `SELECT`, `UPDATE`, `ALTER`, `DELETE`, `DROP`, `JOIN`  
- **Custom query parsing:**  
Manually implemented parser, handling whitespace variability and case-insensitive SQL keywords, without using external parser generators.
- **Robust error handling:** Returns `[OK]` or `[ERROR]` responses for all commands.  
- **Relationship management:** Supports foreign keys and ensures unique, immutable record IDs.  
- **Flexible in-memory representation:** Tables, rows, columns, and relationships represented with custom Java classes.  


## Tech Stack

| Category | Tools / Languages |
|----------|-----------------|
| Language | Java 17 |
| Build | Maven |


## How to Run

1. Build the project using Maven:  
```bash
   mvnw clean compile
```
2. Run the server: 
```bash
   mvnw exec:java@server
```
3. Connect the client:
```bash
  mvnw exec:java@client
```

## Example Queries
```sql
CREATE DATABASE markbook;
USE markbook;
CREATE TABLE marks (name, mark, pass);
INSERT INTO marks VALUES ('Simon', 65, TRUE);
INSERT INTO marks VALUES ('Sion', 55, TRUE);
SELECT * FROM marks;
SELECT * FROM coursework;
JOIN coursework AND marks ON submission AND id;
UPDATE marks SET mark = 38 WHERE name == 'Chris';
DELETE FROM marks WHERE mark<40;
```



## Challenges & Learning Outcomes

- Edge case handling: Designed and implemented handling for a wide range of edge cases, including:

  - Empty tables or columns
  - Queries with unusual spacing or formatting
  - Invalid commands or mismatched data
  - Operations on non-existent tables or columns
- Custom parser implementation: Built a parser from scratch following the provided BNF grammar, without relying on external parser generators.
- Robust error handling: Ensured server stability by returning meaningful [ERROR] messages without crashing, even under unexpected inputs.
- Database design thinking: Created a flexible in-memory structure to represent tables, rows, columns, and relationships, and managed foreign keys with unique, immutable record IDs.
- Persistent storage management: Implemented reliable reading and writing of .tab files to maintain data across server restarts.

## Notes

- Query parsing follows the provided BNF grammar.
- All test scripts were provided in the folder; additional edge case testing was designed and handled in my implementation.
