# Simple Arithmetic Sheet

A minimal spreadsheet web app: **Java 17 + Spring Boot** backend with an **8×5** `String[][]` grid, and a **vanilla HTML/CSS/JavaScript** frontend.

## Features

- Grid columns **A–E**, rows **1–8**
- Formula bar and in-cell editing
- Formulas starting with `=` (e.g. `=A1+B1`, `=A1*B2-C3`)
- Operators: `+`, `-`, `*`, `/` (with parentheses)
- Circular references reported as `#CYCLE!`

## Requirements

- Java 17 or newer
- Maven 3.6+ (or use the Maven Wrapper if present)

## Run the application

From the project directory `simple-arithmetic-sheet`:

```bash
mvn spring-boot:run
```

On Windows (PowerShell):

```powershell
mvn spring-boot:run
```

When the server is up, open:

**http://localhost:8080**

Static files (`index.html`, `styles.css`, `app.js`) are served from Spring Boot’s `classpath:/static/` folder.

## API

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/cells` | Full grid: each cell has `input` (stored value/formula) and `display` (computed) |
| `POST` | `/api/cells/update` | JSON body: `{ "row": 0, "column": 0, "value": "=A1+B1" }` — returns updated grid |

Row and column are **zero-based** (`A1` → `row: 0`, `column: 0`).

## Project layout

```
src/main/java/com/example/sheet/
  SimpleArithmeticSheetApplication.java
  service/SpreadsheetService.java    # String[][] data grid
  service/FormulaEvaluator.java    # formula parsing & arithmetic
  web/CellController.java
src/main/resources/static/
  index.html, styles.css, app.js
```
