# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kairos is a Java-based timesheet/attendance management system built with Spring Boot 3.5.3 and Java 21. The project focuses on managing work reports (勤怠表) and location tracking with features for:
- Timesheet management: tracking work time, leave types, and report status
- Location tracking: GPS coordinate recording for attendance/timesheet purposes only
- Clean Architecture implementation with Domain-Driven Design principles

**Location Data Usage Policy:**
- Location information is collected and used EXCLUSIVELY for timesheet/attendance report generation
- Advanced location features (mapping, analytics, geofencing, etc.) are not part of the system requirements
- Location data should only support basic CRUD operations and integration with timesheet functionality

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

### Domain-Driven Design (DDD) Principles

**Bounded Context Isolation:**
- Each domain package (reports, locations, rules, security) represents a distinct bounded context
- **NO SHARING OF DOMAIN MODELS BETWEEN CONTEXTS**: Each bounded context must maintain its own domain models
- Cross-context communication must occur only through well-defined interfaces and anti-corruption layers
- Domain entities, value objects, and domain services must not be imported across domain boundaries

**Anti-Corruption Layer Pattern:**
- When one bounded context needs data from another, use application-level integration
- Create domain-specific DTOs or value objects within each context
- Use domain services or application services to coordinate between contexts
- Never directly reference domain models from other bounded contexts

**Context Independence:**
- Each bounded context should be independently deployable and testable
- Domain logic must not depend on external contexts
- Changes in one context should not require changes in other contexts (except at integration points)

**Cross-Context Integration Patterns:**
- Use domain service interfaces to abstract external dependencies (Anti-Corruption Layer pattern)
- Each domain defines its own domain-specific value objects (e.g., User) even if they appear similar
- Cross-context communication should occur through application services, not domain services directly
- Avoid sharing concrete domain models; use domain service abstractions instead

**Example: Reports-Locations Integration**
```java
// Good: Reports domain defines its own abstraction
public interface LocationService {
    List<LocalDateTime> getLocationRecordTimes(YearMonth yearMonth, User user);
}

// Bad: Direct import from another domain
import com.github.okanikani.kairos.locations.domains.models.entities.Location;
```

### Package Structure
The project follows Clean Architecture with clear separation of concerns:

- `com.github.okanikani.kairos.reports` - Attendance reports domain package
- `com.github.okanikani.kairos.locations` - Location tracking domain package
- `com.github.okanikani.kairos.rules` - Work rules management domain package
- `com.github.okanikani.kairos.reportcreationrules` - Report creation rules domain package
- `com.github.okanikani.kairos.security` - Security and authentication components

Each domain follows the same structure:
- `domains/models/` - Core domain models (entities, value objects, repositories)
- `domains/service/` - Domain services
- `applications/usecases/` - Application layer use cases
- `others/` - Interface Adapters layer (controllers, repository implementations)

### Key Domain Concepts

**Reports Domain:**
- **Report**: Core entity representing a monthly timesheet with owner, status, work days, and summary
- **Detail**: Represents individual work day information
- **Summary**: Aggregated information for a report period
- **LeaveType**: Enumeration of different leave types
- **ReportStatus**: Current state of a report (draft, submitted, approved, etc.)

**Locations Domain:**
- **Location**: Core entity representing GPS coordinates (latitude, longitude) with timestamp
- GPS coordinate validation ensures latitude (-90.0 to 90.0) and longitude (-180.0 to 180.0) ranges
- **IMPORTANT: Single Purpose Design** - Location data is ONLY used for timesheet/attendance report generation
- Location features beyond basic CRUD and timesheet integration are NOT required for this system
- Advanced features like geographical range search, location analytics, or mapping are out of scope

**Rules Domain:**
- **WorkRule**: Core entity representing work rules for specific workplace locations
- **DefaultWorkRule**: Core entity representing default work rules without membership period constraints
- Comprehensive validation for GPS coordinates, time logic, and period validation
- **Membership period overlap prevention**: Same user cannot have overlapping membership periods

**Report Creation Rules Domain:**
- **ReportCreationRule**: Core entity representing timesheet creation rules for users
- Contains user, calculation start day of month, and time calculation unit in minutes
- User is mandatory and must be unique (one rule per user)

### Technology Stack
- Java 21
- Spring Boot 3.5.3 (Web, Security, Data JPA, Validation)
- PostgreSQL 42.7.3
- Spring Data JPA for ORM and database access
- Maven for build management
- JWT (JSON Web Token) for authentication using jjwt library 0.12.3
- Spring Security for authorization and security configuration

## Language Usage Guidelines
This project follows specific language conventions for different contexts:

**Japanese Usage:**
- User communication and conversations
- Git commit messages and descriptions
- Code comments and inline documentation
- Test method names for business requirement clarity

**English Usage:**
- Claude's internal thinking and reasoning processes
- CLAUDE.md documentation and project guidelines
- Code implementation (variable names, method names, class names)
- API endpoints and technical specifications

## Code Style and Patterns
- The project uses Java Records for immutable domain models
- Null safety is enforced through `Objects.requireNonNull()` validation
- Japanese comments and documentation are used throughout the codebase
- Domain models include comprehensive validation in constructors
- Use `@Service` annotation for use cases and domain services
- Use `@Repository` annotation for repository implementations
- Use `@RestController` for REST API controllers

### REST API Controller Best Practices
- Always specify `name` property in `@PathVariable` annotations when multiple path variables have the same type
- This prevents ambiguity in parameter mapping and potential runtime errors

```java
// Good: Explicit parameter mapping
@DeleteMapping("/{year}/{month}")
public ResponseEntity<Void> deleteReport(
    @PathVariable(name = "year") int year,
    @PathVariable(name = "month") int month,
    Authentication authentication) {

// Bad: Ambiguous parameter mapping (can cause errors)
@DeleteMapping("/{year}/{month}")
public ResponseEntity<Void> deleteReport(
    @PathVariable int year,
    @PathVariable int month,
    Authentication authentication) {
```

## Security Implementation
- JWT authentication is implemented using jjwt library
- API endpoints require authentication except `/api/auth/**`
- User ID is extracted from JWT token in controllers using `Authentication.getName()`
- Stateless session management is configured for REST API

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

## Additional Documentation

@docs/DEVELOPMENT.md
@docs/API.md