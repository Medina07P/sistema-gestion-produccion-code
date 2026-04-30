# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Desktop Java application for managing agricultural harvest workers ("recolectores"). Handles worker registration, daily weighings, and payroll settlements. Uses Swing UI with a MySQL backend via Hibernate/JPA.

## Build & Run

```bash
# Compile
mvn clean compile

# Build fat JAR
mvn clean package

# Run
java -jar target/sistema-recolectores-1.0-SNAPSHOT.jar
```

No test suite exists in this project.

## Database Setup

Requires MySQL 8.x running at `localhost:3306`. Default credentials are `root`/`root123` (configured in `src/main/resources/META-INF/persistence.xml`). Run `src/main/resources/schema.sql` to initialize the database `recolectores_db`. Hibernate is set to `hbm2ddl.auto: update`, so schema evolves automatically during development.

**Default admin login**: username `admin`, password `Admin1234`.

## Architecture

Strict 3-layer architecture with manual dependency injection wired in `MainFrame` constructor:

```
UI (Swing panels)  →  Service interfaces  →  DAO interfaces  →  Hibernate EntityManager
```

- **`com.sistema.model`** — JPA entities (`@Entity`)
- **`com.sistema.dao`** — Repository interfaces extending `IGenericDAO<T, ID>`; implementations use `EntityManager` directly
- **`com.sistema.service`** — Business logic layer; throws `NegocioException` (unchecked) for rule violations caught by UI via `JOptionPane`
- **`com.sistema.ui`** — `MainFrame` holds a `JTabbedPane` with one panel per module; `LoginFrame` blocks startup
- **`com.sistema.report`** — Report generators implementing `IReporteGenerador`
- **`com.sistema.util`** — `ConexionBD` (EntityManagerFactory singleton), `Validador` (static validation helpers)

## Domain Model Key Relationships

- **Recolector** belongs to a **Lote** (ManyToOne, nullable)
- **Pesaje** (daily weighing) → Recolector + Usuario (who recorded it); lote is derived via `pesaje.getRecolector().getLote()`
- **Liquidacion** (payroll settlement) → many **LiquidacionDetalle** (one per recolector with total kilos and payment)
- **Pago** is a transient DTO (not persisted) used only for UI display in `PagoPanel`

## Important Technical Notes

- Uses **Hibernate 6 / Jakarta EE** (`jakarta.persistence.*`), not the older `javax.persistence.*`
- `LiquidacionDetalle` must be listed in `persistence.xml` entity list — check if it's missing when adding new entities
- The `Pago` class is not an `@Entity`; it is an in-memory calculation object
- All modules in `MainFrame` receive their services via constructor injection — no DI framework
- Business rule errors surface as `NegocioException` messages shown in `JOptionPane.showMessageDialog`
- Application exits immediately if MySQL is unavailable at startup (no retry/fallback)
