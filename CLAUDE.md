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
The project follows Clean Architecture with clear separation of concerns:

- `com.github.okanikani.kairos.reports` - Main domain package for attendance reports
  - `domains/models/` - Core domain models
    - `entities/` - Domain entities (Report)
    - `vos/` - Value objects (User, Summary, Detail, WorkTime)
    - `constants/` - Enums (LeaveType, ReportStatus)
    - `repositories/` - Repository interfaces
  - `domains/service/` - Domain services (SummaryFactory)
  - `domains/roundings/` - Rounding logic for time calculations
  - `applications/usecases/` - Application layer use cases
    - `dto/` - Data Transfer Objects for use case input/output
    - `mapper/` - Mappers between DTOs and domain objects
  - `others/` - Interface Adapters layer
    - `controllers/` - REST API controllers
    - `repositories/` - Repository implementations
- `com.github.okanikani.kairos.security` - Security and authentication components

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
- JWT (JSON Web Token) for authentication using jjwt library 0.12.3
- Spring Security for authorization and security configuration

## Development Notes

### Code Style and Patterns
- The project uses Java Records for immutable domain models
- Null safety is enforced through `Objects.requireNonNull()` validation
- Japanese comments and documentation are used throughout the codebase
- Domain models include comprehensive validation in constructors
- Use `@Service` annotation for use cases and domain services
- Use `@Repository` annotation for repository implementations
- Use `@RestController` for REST API controllers

### Testing Guidelines
- Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)` for unit tests
- Use `@MockitoBean` annotation has been deprecated since Spring Boot 3.4; use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito` instead
- Mock dependencies using Mockito for isolated unit testing
- Test classes should follow the naming convention: `{ClassUnderTest}Test`
- Test method names should be descriptive in Japanese: `execute_正常ケース_期待される結果`

### Security Implementation
- JWT authentication is implemented using jjwt library
- API endpoints require authentication except `/api/auth/**`
- User ID is extracted from JWT token in controllers using `Authentication.getName()`
- Stateless session management is configured for REST API

## API Design

### REST API Endpoints
- **Authentication**: POST `/api/auth/login` - Returns JWT token
- **Reports**: 
  - POST `/api/reports` - Register new timesheet report
  - GET `/api/reports/{year}/{month}` - Get timesheet report by year/month
- All API endpoints except authentication require JWT token in Authorization header: `Bearer {token}`
- Year and month are passed as path parameters, user ID is extracted from JWT token
- Request/response bodies use DTOs for data transfer between layers

### HTTP Status Codes
- 200 OK: Successful retrieval
- 201 Created: Successful creation
- 400 Bad Request: Invalid request parameters
- 401 Unauthorized: Missing or invalid JWT token
- 403 Forbidden: User not authorized for requested resource
- 404 Not Found: Resource not found
- 500 Internal Server Error: Server error

## Dependency Management

### Known Issues and Solutions
- When adding JWT dependencies, exclude `jackson-databind` from `jjwt-jackson` to avoid version conflicts:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
    <exclusions>
        <exclusion>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## Commit Message Guidelines

Follow Conventional Commits specification (https://www.conventionalcommits.org/ja/v1.0.0/#%e4%bb%95%e6%a7%98):
- Use format: `<type>: <description>`
- Common types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
- Write descriptions in Japanese for consistency with codebase
- Example: `docs: CLAUDE.mdファイル更新 - Java/Spring Boot開発ガイド追加`