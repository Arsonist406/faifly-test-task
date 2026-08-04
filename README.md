# Faifly Test Task

**Author:** Ostap Seniv — ostap06seniv@gmail.com
**Task:** https://docs.google.com/document/d/1bjiLWD3a8ZCc8gQ0bO-ujGIOjlpLGDWSk5NvBaI9yKg/edit?usp=sharing

## Run

```bash
docker compose up --build
```

With performance dataset (hundreds of thousands of rows):

```bash
APP_PROFILE=perf docker compose up --build
```
[
]()App: `http://localhost:8080`

## Data

- Schema + demo data migrations: `src/main/resources/db/migration/`
- Performance dataset migration (used by the `perf` profile): `src/main/resources/db/perf/`
- Database dump: `db/dump/faifly_testdata.sql`
