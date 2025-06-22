# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kairos is a Java-based timesheet/attendance management system built with Spring Boot 3.5.3 and Java 21. The project focuses on managing work reports (勤怠表) with features for tracking work time, leave types, and report status management.

## Build and Development Commands

### Building the Project
```bash
cd kairos-backend
mvn clean compile
```

### Running the Application
```bash
cd kairos-backend
mvn spring-boot:run
```

### Running Tests
```bash
cd kairos-backend
mvn test
```

### Packaging
```bash
cd kairos-backend
mvn clean package
```

## Architecture

The codebase follows Domain-Driven Design (DDD) principles with clear separation of concerns:

### Package Structure
- `com.github.okanikani.kairos.reports` - Main domain package for attendance reports
  - `domains/models/` - Core domain models
    - `entities/` - Domain entities (Report)
    - `vos/` - Value objects (User, Summary, Detail, WorkTime)
    - `constants/` - Enums (LeaveType, ReportStatus)
    - `repositories/` - Repository interfaces
  - `domains/service/` - Domain services (SummaryFactory)
  - `domains/roundings/` - Rounding logic for time calculations
  - `applications/usecases/` - Application layer use cases

### Key Domain Concepts
- **Report**: Core entity representing a monthly timesheet with owner, status, work days, and summary
- **Detail**: Represents individual work day information
- **Summary**: Aggregated information for a report period
- **LeaveType**: Enumeration of different leave types
- **ReportStatus**: Current state of a report (draft, submitted, approved, etc.)

### Technology Stack
- Java 21
- Spring Boot 3.5.3 (Web, Security)
- PostgreSQL 42.7.3
- Maven for build management

## Development Notes

- The project uses Java Records for immutable domain models
- Null safety is enforced through `Objects.requireNonNull()` validation
- Japanese comments and documentation are used throughout the codebase
- Domain models include comprehensive validation in constructors

## Commit Message Guidelines

Follow Conventional Commits specification (https://www.conventionalcommits.org/ja/v1.0.0/#%e4%bb%95%e6%a7%98):
- Use format: `<type>: <description>`
- Common types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
- Write descriptions in Japanese for consistency with codebase
- Example: `docs: CLAUDE.mdファイル更新 - Java/Spring Boot開発ガイド追加`