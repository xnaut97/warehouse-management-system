# Warehouse Management System

A Spring Boot REST API for managing a two-tier warehouse operation: raw materials moving in and out of storage, finished products built from those materials via a bill of materials, periodic stocktaking, and the reporting layer on top of both.

- **Java 21** / **Spring Boot 4.1**
- **MySQL 8.4** with JPA/Hibernate
- **JWT** authentication with role-based authorization
- **OpenAPI/Swagger** documentation

## Table of contents

- [Domain overview](#domain-overview)
- [Roles](#roles)
- [Getting started](#getting-started)
  - [Run with Docker Compose](#run-with-docker-compose)
  - [Run locally](#run-locally)
- [Seed data](#seed-data)
- [Configuration](#configuration)
- [Seed accounts](#seed-accounts)
- [Authentication](#authentication)
- [API reference](#api-reference)
- [Project layout](#project-layout)
- [Testing](#testing)

## Domain overview

The system tracks two separate stock ledgers that share one transaction history.

**Materials** are received from suppliers (`GoodsReceipt`) and issued to production or customers (`GoodsIssue`). Stock is held per warehouse in `MaterialInventory`.

**Products** are received from production (`ProductReceipt`) and issued to customers (`ProductIssue`), with stock in `ProductInventory`. A `BOM` (bill of materials) describes which materials each product consumes.

Every confirmed receipt or issue writes an `InventoryTransaction` (`IN`, `OUT`, or `ADJUSTMENT`), which is what the stock cards and reports read from. Documents get a human-readable number from a `DocumentSequence`, prefixed by type — `GR`, `GI`, `PR`, `PI`, `IC`, `ADJ`.

**Stocktaking** compares recorded stock against a physical count. A session is confirmed and then balanced; balancing emits adjustment transactions for any variance.

**Alerts** are derived rather than stored as workflow state: `BELOW_MIN` and `ABOVE_MAX` for threshold breaches, `NEAR_EXPIRY` for lot quality risk, and `STOCKTAKING_VARIANCE` for internal control.

**Audit logging** is cross-cutting — service methods annotated with `@Audit` are intercepted by `AuditAspect` and written to `AuditLog`.

## Roles

| Role | Scope |
| --- | --- |
| `ADMIN` | Everything, including user management and deletes |
| `WAREHOUSE_MANAGER` | Full operational control: master data, documents, stocktaking |
| `WAREHOUSE_STAFF` | Read master data and stock; limited document participation |
| `ACCOUNTANT` | Read documents, inventory, and reports |
| `EXECUTIVE_BOARD` | Read documents, inventory, reports, and dashboard |

Authorization is enforced per endpoint with `@PreAuthorize`.

## Getting started

### Run with Docker Compose

The compose file starts MySQL and the application together. It reads datasource settings from the environment, so create a `.env` next to `docker-compose.yml` first:

```bash
printf 'SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/warehouse_management\nSPRING_DATASOURCE_USERNAME=root\nSPRING_DATASOURCE_PASSWORD=root\n' > .env
```

Then bring the stack up:

```bash
docker compose up --build
```

The API is on `http://localhost:8080`. MySQL is published on host port **3307** (container port 3306) so it will not collide with a local MySQL install.

### Run locally

You need JDK 21 and a MySQL 8 instance holding a `warehouse_management` database. With the defaults in `application.yml` (`localhost:3306`, `root`/`root`) no extra configuration is needed:

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd`. To build a jar instead:

```bash
./mvnw clean package
```

Schema is managed by Hibernate (`ddl-auto: update`), and `src/main/resources/schema.sql` runs on every startup to apply idempotent migrations to existing installs. Roles, seed users, and a default warehouse are created on first boot by `DataInitializer`.

## Seed data

`DataInitializer` runs on every startup and drives the seeders in
`src/main/java/com/github/xnaut97/wms/seed/`:

| Seeder | Creates | Re-run behaviour |
| --- | --- | --- |
| `RoleSeeder`, `UserSeeder` | One role and one account per `RoleType` | Skips existing rows by role / username |
| `WarehouseSeeder` | Material and product warehouses | Skips existing codes |
| `SupplierSeeder`, `CustomerSeeder` | Suppliers by material group, customers by group | Skips existing codes |
| `MaterialSeeder` | Sand, cement, HPMC, RDP and one packaging SKU per product | Skips existing codes |
| `ProductSeeder` | The `TP-*` tile-adhesive SKUs | Skips existing codes |
| `BOMSeeder` | One BOM per product, mixing ratios derived from the recipe | Skips products that already have a BOM |
| `ProductionOperationSeeder` | ~13 months of operational history | Skips entirely once the primary product holds stock |
| `SeedValidationReporter` | Nothing — validates and prints a PASS/FAIL table | Always runs |

### What the operation seeder generates

`ProductionOperationSeeder` replays the full business chain day by day, ending on
the current date, and calls the same services the UI calls
(`ReceiptService`, `IssueService`, `ProductReceiptService`, `ProductIssueService`,
`StocktakingService`) rather than inserting rows directly. Inventory updates,
stock validation, average-cost recalculation, lot creation and FEFO ordering are
therefore the production code paths, not seeder copies.

Per working day:

1. A production plan of 800–900 bags, weighted so the premium `Keo 2` SKU takes
   5–10% of annual volume and is produced in short campaigns rather than daily.
2. Material requirements read from the **database BOM** for each product — no
   formula is hardcoded in the seeder. Actual issue quantity is the BOM standard
   plus a random draw bounded by that BOM line's `max_waste_ratio`.
3. Material receipts raised beforehand whenever stock would not cover the issue,
   respecting per-material packaging (sand in 38,400 kg containers, HPMC in
   25 kg bags, RDP in 20 kg bags) with drifting purchase prices so the weighted
   average cost is meaningful.
4. A production material issue, then a finished-product receipt for exactly the
   planned quantity, with a lot number and expiry of production date + 1 year.
5. Seasonal sales that consume lots in expiry order through
   `ProductStockService.getAvailableLots`.
6. Stocktaking sessions, 4–8 per month, mostly matching with a minority carrying
   a ±0.5–3% variance and a reason.

Finished-product receipt cost is **derived**, not seeded: each line is priced at
that batch's own material issue value divided by the bags produced, so the chain
runs material receipt → `MaterialService.recalculateAveragePrice` → issue value →
batch cost. `ProductService.recalculateAveragePrice` then maintains
`products.average_price` from confirmed sales, unchanged by the seeder.

### Alert demo conditions

The seeder deliberately drives HPMC, RDP and the 20 kg packaging SKU below their
minimum stock, pushes sand above its maximum with a final bulk delivery, and
holds a small reserve on three production dates so those lots survive to sit
60–90 days from expiry. Nothing is written to an alert table — `AlertService`
derives all four alert types from this data at request time.

### Re-running

Master data is idempotent by code, so restarting the application is safe. The
operation seeder is all-or-nothing: it skips as soon as the primary product has
stock, so it will not top up a partially seeded database. To regenerate the
operational history, drop and recreate the schema:

```bash
mysql -uroot -proot -e "DROP DATABASE warehouse_management; CREATE DATABASE warehouse_management CHARACTER SET utf8mb4;"
```

A full run takes roughly 10–15 minutes and produces on the order of 250 material
receipts, 350 production issues and receipts, 1,200 sales issues and 70
stocktaking sessions. Nothing in the seeder deletes or resets data on its own.

### Validation

`SeedValidationReporter` runs last and prints a PASS/FAIL line per check —
negative inventory, BOM standard versus actual consumption against the allowed
wastage, production plan and product mix, lot and expiry integrity, FEFO order,
the inventory balance equation, the costing chain, stocktaking coverage and the
alert conditions. It queries the database directly, so it reports on whatever is
actually stored rather than on what the seeder intended.

## Configuration

All settings live in `src/main/resources/application.yml` and can be overridden by environment variable.

| Variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/warehouse_management` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `root` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `root` | Database password |
| `CORS_ALLOWED_ORIGINS` | Vercel frontend plus `localhost:5173` / `localhost:3000` | Comma-separated allowed origins |
| `PORT` | `8080` | HTTP port |

Because CORS runs with credentials enabled, `CORS_ALLOWED_ORIGINS` must list explicit origins — a `*` wildcard is rejected at startup.

> **Note:** `jwt.secret` in `application.yml` is a checked-in development value. Override it before deploying anywhere real.

## Seed accounts

`UserSeeder` creates one account per role on first startup. The password is the username followed by `123`.

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `admin123` | `ADMIN` |
| `manager` | `manager123` | `WAREHOUSE_MANAGER` |
| `staff` | `staff123` | `WAREHOUSE_STAFF` |
| `accountant` | `accountant123` | `ACCOUNTANT` |
| `board` | `board123` | `EXECUTIVE_BOARD` |

These are development credentials. Change them before exposing the API.

## Authentication

Log in to get a JWT, then send it as a bearer token on every subsequent request.

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

```bash
curl http://localhost:8080/api/materials -H "Authorization: Bearer <token>"
```

Tokens are valid for 24 hours. Sessions are stateless — there is no server-side session and no refresh token.

All responses are wrapped in a common envelope:

```json
{ "success": true, "message": "Login successful", "data": {} }
```

## API reference

Interactive documentation is served once the app is running:

- Swagger UI — `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON — `http://localhost:8080/api-docs`

`/api/auth/**` and the documentation routes are public; everything else requires a token.

### Endpoint groups

| Prefix | Description |
| --- | --- |
| `/api/auth` | Login |
| `/api/users` | User administration (`ADMIN` only) — create, update, lock/unlock, reset password |
| `/api/warehouses` | Warehouses |
| `/api/materials` | Material master data |
| `/api/products` | Product master data |
| `/api/suppliers`, `/api/customers` | Business partners |
| `/api/boms` | Bills of materials |
| `/api/receipts`, `/api/issues` | Material goods receipts and issues, with line items and confirmation |
| `/api/product-receipts`, `/api/product-issues` | Product receipts and issues |
| `/api/inventories`, `/api/product-inventories` | Current stock, lots, low-stock lookups |
| `/api/inventory-transactions` | Movement history |
| `/api/stock-cards` | Per-item stock card |
| `/api/stocktaking` | Stocktaking sessions: items, batches, confirm, balance |
| `/api/alerts` | Threshold, expiry, and variance alerts |
| `/api/audit-logs` | Audit trail |
| `/api/dashboard` | Summary, trends, variance, and decision-support widgets |
| `/api/statistics` | Aggregate counters |
| `/api/reports/*` | Inventory, inventory value, receipts, issues, operations, stocktaking |

## Project layout

```
src/main/java/com/github/xnaut97/wms/
├── annotation/   @Audit marker
├── aspect/       AuditAspect — writes the audit trail
├── config/       CORS, JPA, JWT, OpenAPI, data initialization
├── controller/   REST endpoints, grouped by domain
├── dto/          Request/response models
├── entity/       JPA entities
├── enums/        Domain enums (roles, statuses, document types)
├── exception/    Exception types and global handler
├── factory/      Sample data factory used by seeders
├── repository/   Spring Data repositories
├── security/     JWT filter, user details, security configuration
├── seed/         Master data and operational seeders, plus seed validation
└── service/      Business logic
```

## Testing

```bash
./mvnw test
```

The suite covers CORS configuration and preflight behaviour, and stock validation for material and product issues.
