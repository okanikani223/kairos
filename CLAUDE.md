# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kairos is a Java-based timesheet/attendance management system built with Spring Boot 3.5.3 and Java 21. The project focuses on managing work reports (勤怠表) and location tracking with features for:
- Timesheet management: tracking work time, leave types, and report status
- Location tracking: GPS coordinate recording with validation
- Clean Architecture implementation with Domain-Driven Design principles

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
  - `domains/models/` - Core domain models
    - `entities/` - Domain entities (Report)
    - `vos/` - Value objects (User, Summary, Detail, WorkTime)
    - `constants/` - Enums (LeaveType, ReportStatus)
    - `repositories/` - Repository interfaces (ReportRepository)
  - `domains/service/` - Domain services (SummaryFactory)
  - `domains/roundings/` - Rounding logic for time calculations
  - `applications/usecases/` - Application layer use cases
    - `dto/` - Data Transfer Objects for use case input/output
    - `mapper/` - Mappers between DTOs and domain objects
  - `others/` - Interface Adapters layer
    - `controllers/` - REST API controllers (ReportController)
    - `repositories/` - Repository implementations (InMemoryReportRepository)

- `com.github.okanikani.kairos.locations` - Location tracking domain package
  - `domains/models/` - Core domain models
    - `entities/` - Domain entities (Location)
    - `repositories/` - Repository interfaces (LocationRepository)
  - `applications/usecases/` - Application layer use cases
    - `dto/` - Data Transfer Objects (RegisterLocationRequest, LocationResponse)
  - `others/` - Interface Adapters layer
    - `controllers/` - REST API controllers (LocationController)

- `com.github.okanikani.kairos.rules` - Work rules management domain package
  - `domains/models/` - Core domain models
    - `entities/` - Domain entities (WorkRule, DefaultWorkRule)
    - `repositories/` - Repository interfaces (WorkRuleRepository, DefaultWorkRuleRepository)
    - `vos/` - Value objects (User)
  - `domains/service/` - Domain services (WorkRuleDomainService)
  - `applications/usecases/` - Application layer use cases
    - `dto/` - Data Transfer Objects (RegisterWorkRuleRequest, WorkRuleResponse, etc.)
    - `mapper/` - Mappers between DTOs and domain objects (WorkRuleMapper, DefaultWorkRuleMapper)
  - `others/` - Interface Adapters layer
    - `controllers/` - REST API controllers (WorkRuleController, DefaultWorkRuleController)
    - `repositories/` - Repository implementations (InMemoryWorkRuleRepository, InMemoryDefaultWorkRuleRepository)

- `com.github.okanikani.kairos.reportcreationrules` - Report creation rules domain package
  - `domains/models/` - Core domain models
    - `entities/` - Domain entities (ReportCreationRule)
    - `repositories/` - Repository interfaces (ReportCreationRuleRepository)
    - `vos/` - Value objects (User)
  - `applications/usecases/` - Application layer use cases
    - `dto/` - Data Transfer Objects (RegisterReportCreationRuleRequest, ReportCreationRuleResponse, UserDto)
    - `mapper/` - Mappers between DTOs and domain objects (ReportCreationRuleMapper)
  - `others/` - Interface Adapters layer
    - `controllers/` - REST API controllers (ReportCreationRuleController)
    - `repositories/` - Repository implementations (InMemoryReportCreationRuleRepository)

- `com.github.okanikani.kairos.security` - Security and authentication components
  - Authentication controllers, JWT services, security configuration

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

**Rules Domain:**
- **WorkRule**: Core entity representing work rules for specific workplace locations
- Contains workplace ID, GPS coordinates, user assignment, standard work hours, break times, and membership period
- Comprehensive validation for GPS coordinates, time logic, and period validation
- Supports flexible break time configuration (can be null for no break periods)
- **DefaultWorkRule**: Core entity representing default work rules without membership period constraints
- Similar to WorkRule but without start/end dates, providing more flexible rule configuration
- Used as fallback or template rules for workplace locations

**WorkRule Business Rules:**
- Workplace ID is mandatory
- GPS coordinates must be within valid ranges (latitude: -90.0 to 90.0, longitude: -180.0 to 180.0)
- User, standard work times, and membership period are required
- Standard start time must be before standard end time
- Break start time must be before break end time (when configured)
- Membership start date must be before or equal to membership end date
- Break times must be configured as both start and end times or both null (partial configuration not allowed)
- **Membership period overlap prevention**: Same user cannot have overlapping membership periods across multiple work rules

**DefaultWorkRule Business Rules:**
- Similar validation rules as WorkRule except no membership period constraints
- Same user and workplace combination can only have one default work rule
- Used for establishing baseline work rule configurations

**Report Creation Rules Domain:**
- **ReportCreationRule**: Core entity representing timesheet creation rules for users
- Contains user, calculation start day of month, and time calculation unit in minutes
- Comprehensive validation for day range (1-31) and time unit range (1-60 minutes)
- Used for determining timesheet calculation periods and time rounding rules

**ReportCreationRule Business Rules:**
- User is mandatory and must be unique (one rule per user)
- Calculation start day must be between 1 and 31 (accommodates various month-end patterns)
- Time calculation unit must be between 1 and 60 minutes for flexible time rounding
- Used for establishing user-specific timesheet creation and calculation settings

### Technology Stack
- Java 21
- Spring Boot 3.5.3 (Web, Security, Data JPA, Validation)
- PostgreSQL 42.7.3
- Spring Data JPA for ORM and database access
- Hibernate as JPA implementation provider
- Maven for build management
- JWT (JSON Web Token) for authentication using jjwt library 0.12.3
- Spring Security for authorization and security configuration

## Development Notes

### CLAUDE.md Update Guidelines
When creating new features or updating existing functionality, you must update the relevant sections in CLAUDE.md:

**Sections requiring updates:**
- Package Structure - when new domain packages are added or existing structure changes
- Key Domain Concepts - when new entities, business rules are added or existing ones are modified
- REST API Endpoints - when new API endpoints are added or existing endpoints are modified
- Technology Stack - when new dependencies or libraries are added or versions are updated

**Purpose:**
- Maintain project documentation consistency
- Provide accurate information for new developers
- Enable Claude Code to understand the latest project status

### Language Usage Guidelines
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

**Rationale:**
- Japanese ensures clear communication with Japanese stakeholders and business requirements
- English maintains international development standards and global accessibility
- Consistent language switching prevents confusion and maintains professional standards

### Code Style and Patterns
- The project uses Java Records for immutable domain models
- Null safety is enforced through `Objects.requireNonNull()` validation
- Japanese comments and documentation are used throughout the codebase
- Domain models include comprehensive validation in constructors
- Use `@Service` annotation for use cases and domain services
- Use `@Repository` annotation for repository implementations
- Use `@RestController` for REST API controllers

#### REST API Controller Best Practices
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

### Code Comment Best Practices
Based on Stack Overflow's best practices for writing code comments (https://stackoverflow.blog/2021/12/23/best-practices-for-writing-code-comments/), this project follows these guidelines:

#### 1. Comments Should Tell You "Why", Not "What"
- Code tells you HOW, comments tell you WHY
- Focus on business rules, domain knowledge, and design decisions
- Avoid comments that simply restate what the code is doing

```java
// Good: Explains business rule
// 業務ルール: 同一ユーザー・同一年月の勤怠表は重複登録不可
// 理由: 給与計算の二重処理やデータ不整合を防止するため
Report existingReport = reportRepository.find(request.yearMonth(), user);

// Bad: Just restates the code
// ユーザーIDからJWTトークンを生成
String token = jwtService.generateToken(userId);
```

#### 2. Good Comments vs Bad Comments
- **Good**: Explain complex business logic, magic numbers, workarounds, TODOs
- **Bad**: Duplicate method names, obvious operations, excuse unclear code

#### 3. Key Rules to Follow
1. **Don't duplicate code**: If the method name clearly states what it does, don't repeat it in comments
2. **Don't excuse unclear code**: Make code clearer instead of adding explaining comments
3. **Explain unidiomatic code**: When you must use unusual patterns, explain why
4. **Add context for magic numbers**: Always explain constants and configuration values
5. **Mark incomplete implementations**: Use TODO/FIXME for temporary solutions
6. **Document bug fixes**: Explain why specific workarounds exist
7. **Provide external references**: Link to specifications, RFCs, or business requirements

#### 4. Comment Examples in This Project

**Business Logic Comments (Good):**
```java
// 特別休暇日数計算: 業務ルールにより部分取得不可のためcount()で固定1日として扱う
// ※企業ポリシー上特別休暇の部分取得が認められていないため
double specialLeave = details.stream()
    .filter(l -> l == LeaveType.SPECIAL_LEAVE)
    .count();
```

**Configuration Comments (Good):**
```java
// JWTトークン有効期限: 86400000ms = 24時間
// 業務要件では1日以内のセッションで再ログインが必要
@Value("${jwt.expiration:86400000}")
private long jwtExpiration;
```

**TODO Comments (Good):**
```java
/**
 * ※これは開発・テスト用の一時的な実装です。
 * 本番環境ではデータベースを使用した実装に置き換える必要があります。
 * TODO: PostgreSQL等を使用した永続化実装への置き換え
 */
@Repository
public class InMemoryReportRepository implements ReportRepository {
```

**Magic Number Explanations (Good):**
```java
// "Bearer " プレフィックス（7文字）を除去してJWTトークンのみを抽出
jwt = authHeader.substring(7);
```

#### 5. When NOT to Comment
- Method names that clearly describe their purpose
- Simple getter/setter operations
- Obvious parameter mappings in DTOs
- Self-explanatory variable assignments
- Standard framework patterns (unless there's a specific business reason)

#### 6. Documentation Standards
- Use Japanese for consistency with the codebase
- Write Javadoc for public APIs that will be used by other developers
- Focus comments on domain-specific knowledge that isn't obvious from code
- Keep comments up-to-date when code changes

### Testing Guidelines
- Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)` for unit tests
- Use `@MockitoBean` annotation has been deprecated since Spring Boot 3.4; use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito` instead
- Mock dependencies using Mockito for isolated unit testing
- Test classes should follow the naming convention: `{ClassUnderTest}Test`
- Test method names should be descriptive in Japanese: `execute_正常ケース_期待される結果`

### Test-Driven Development (TDD)
Based on Kent Beck's canonical approach to TDD (https://t-wada.hatenablog.jp/entry/canon-tdd-by-kent-beck), this project follows TDD principles:

#### TDD Core Workflow
1. **テストリスト作成**: Create a list of expected behaviors to implement
2. **Red**: Write a single, specific test that fails
3. **Green**: Write just enough code to make the test pass
4. **Refactor**: Improve code structure while maintaining test success
5. **Repeat**: Continue until test list is empty

#### TDD Principles
- **プログラミングワークフロー**: TDD is a programming workflow, not just a testing technique
- **インターフェース設計と実装設計の分離**: Separates interface design from implementation design
- **漸進的開発**: Focuses on incremental development and design
- **設計の創発**: Design emerges naturally through the TDD cycle

#### What TDD Is NOT
- Not just about writing tests first
- Not about achieving high code coverage
- Not about skipping design
- Not about writing many tests upfront

#### TDD Benefits
- **過剰設計・不足設計の防止**: Prevents over-designing or under-designing
- **設計の漸進的発見**: Allows design to emerge incrementally
- **システム動作への信頼**: Provides confidence in system behavior
- **既存機能の保護**: Ensures existing functionality remains intact while adding new features

#### TDD Implementation Guidelines
- Start with a clear test list of behaviors to implement
- Write the smallest test that expresses one behavior
- Make the test pass with minimal implementation
- Refactor once tests are green
- Use Japanese for test method names to clearly express business requirements
- Focus on business value and domain logic in tests

#### TDD for Existing Code Modifications
**IMPORTANT: Apply TDD even when modifying existing implementations**

When modifying existing code, follow the TDD workflow:

1. **Test List for Changes**: Create a list of behaviors that need to be modified or added
2. **Red Phase**: Write failing tests that express the new expected behavior
3. **Green Phase**: Modify the existing implementation to make tests pass
4. **Refactor Phase**: Clean up both test and production code while maintaining green tests
5. **Regression Protection**: Ensure existing tests continue to pass to prevent breaking changes

**Examples of modifications requiring TDD:**
- Adding new validation rules to existing entities
- Modifying business logic in domain services
- Changing API endpoint behavior or adding new endpoints
- Updating data transformation logic in mappers
- Enhancing existing use cases with new functionality

**Workflow Example:**
```java
// 1. Add failing test for new requirement
@Test
void validateWorkRule_重複期間の場合_例外が発生する() {
    // Test new overlap validation requirement
}

// 2. Modify existing implementation
public class WorkRule {
    // Add overlap validation logic
}

// 3. Ensure all existing tests still pass
// 4. Refactor if needed while maintaining green tests
```

#### Testing Spectrum (位置づけ)
1. **自動テスト (Automated Testing)**: Basic foundation
2. **開発者テスト (Developer Testing)**: Developers write tests
3. **テストファースト (Test-First Programming)**: Write tests before implementation
4. **テスト駆動開発 (TDD)**: Full TDD workflow with design feedback

#### TDD Implementation Example
The work rule management feature was implemented using complete TDD approach:

**Implementation Flow:**
1. **Test List Creation**: Identified WorkRule entity requirements and created comprehensive test case list
2. **Red Phase**: Implemented WorkRuleTest class first, causing compilation errors (expected)
3. **Green Phase**: Created minimal WorkRule entity implementation to make tests pass
4. **Refactor Phase**: Improved validation logic organization and code readability
5. **Repeat**: Applied same TDD cycle for RegisterWorkRuleUsecase implementation

**Test Coverage:**
- All entity validation rules (12 test cases)
- Use case happy and error paths (4 test cases)
- Boundary value testing (GPS coordinate ranges, time ordering logic)

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
  - PUT `/api/reports/{year}/{month}` - Update existing timesheet report
  - DELETE `/api/reports/{year}/{month}` - Delete timesheet report
  - POST `/api/reports/generate` - Generate timesheet report from location data
- **Locations**:
  - POST `/api/locations` - Register new location data
- **Work Rules**:
  - POST `/api/work-rules` - Register new work rule
- **Default Work Rules**:
  - POST `/api/default-work-rules` - Register new default work rule
- **Report Creation Rules**:
  - POST `/api/report-creation-rules` - Register new report creation rule
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