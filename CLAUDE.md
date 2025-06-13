# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Kairos（カイロス）** is a comprehensive attendance management system designed for the Japanese market. It automates time tracking using location data and supports multiple submission formats for different employers/clients with different deadlines and requirements.

The project is currently in the **design phase** - all core functionality has been documented with comprehensive design specifications, but no implementation code exists yet.

## Architecture & Design Standards

This project follows a layered architecture with strict separation of concerns:

### Architecture Layers
- **Presentation Layer**: API Gateway, Authentication/Authorization middleware
- **Application Layer**: Controllers for each feature (attendance, location, companies)
- **Business Logic Layer**: Services containing core business logic
- **Common Libraries**: Shared algorithms and utilities (work determination, distance calculation, validation)
- **Data Access Layer**: Repositories for data persistence
- **Data Layer**: Database models (User, Company, Location, Attendance, AttendanceDetail, UserAttendanceSetting)

### Key Design Principles
- **RESTful API Design**: Unified endpoint naming, HTTP status codes, and response formats
- **Component Responsibility**: Clear separation between controllers, services, algorithms, and repositories
- **Dependency Management**: Upper layers depend on lower layers, never the reverse
- **Security First**: JWT authentication, HTTPS, encrypted location data, access control
- **Japanese Labor Law Compliance**: 36-hour agreements, flexible working hours, multiple submission formats

## Development Guidelines

### Code Language & Style
- **Primary Language**: Japanese for all documentation, comments, and user-facing content
- **API Documentation**: Follow unified API design standards defined in `doc/guideline/API設計統一ガイドライン.md`
- **Response Format**: Standardized JSON structure with `success`, `data`, `error`, and `meta` fields
- **Error Handling**: Unified error codes and detailed error messages in `details` arrays

### Git Workflow
- **Commit Messages**: Follow [Conventional Commits](https://www.conventionalcommits.org/ja/v1.0.0/) format in Japanese
- **Git Commands**: Use `git --no-pager diff` and `git --no-pager log` for diff and log operations
- **Branch Strategy**: Work on `main` branch for this design-phase project

### Documentation Structure
All documentation follows a unified structure defined in `doc/guideline/ドキュメント構造統一標準.md`:

- **Requirements**: `doc/kairos_要件整理.md` (project requirements)
- **Terminology**: `doc/kairosプロジェクト_用語集.md` (unified term definitions)
- **Design Guidelines**: `doc/guideline/` (API design, security, validation standards)
- **Feature Designs**: `doc/design/` (detailed functional specifications)
- **Architecture**: `doc/design/コンポーネント分割と責務明確化設計書.md`

## Key Design Documents

When implementing features, always reference these core documents:

1. **API Design**: `doc/guideline/API設計統一ガイドライン.md` - Unified API response formats, HTTP status codes, error handling
2. **Component Architecture**: `doc/design/コンポーネント分割と責務明確化設計書.md` - Layer responsibilities, dependencies, data flow
3. **Security Standards**: `doc/guideline/セキュリティ要件記載標準.md` - Authentication, authorization, data protection
4. **Validation Standards**: `doc/guideline/バリデーション仕様記載標準.md` - Input validation rules and error messages
5. **Performance Requirements**: `doc/guideline/パフォーマンス要件記載標準.md` - Response time, throughput, scalability targets

## Feature Implementation Guidelines

Each feature follows the same implementation pattern based on the component architecture:

### CRUD Operations Structure
All features implement standardized CRUD operations:
- **Create**: POST with 201 status, standardized validation
- **Read**: GET with filtering, pagination, search capabilities  
- **Update**: PUT/PATCH with optimistic locking, validation
- **Delete**: Logical deletion preferred, audit trail required

### Core Features (Design Complete)
- **位置情報登録**: GPS-based location tracking with privacy controls
- **勤怠情報CRUD**: Attendance record management (manual/automatic)
- **勤怠情報提出先CRUD**: Company/submission destination management
- **ユーザー勤怠設定**: User-specific attendance settings and preferences

### Business Logic Integration
- **勤務判定アルゴリズム**: Work status determination from location/time data
- **距離計算アルゴリズム**: Haversine formula for location proximity
- **休暇判定ロジック**: Leave status determination from attendance patterns

## Implementation Notes

### Technology Stack Preparation
The project is designed to be technology-agnostic but assumes:
- RESTful API backend with JSON communication
- Database with ACID compliance for attendance data
- Location-based services integration
- JWT-based authentication system

### Quality Assurance
Follow the quality check prompts defined in `doc/guideline/設計品質確保プロンプト戦略.md`:
- API response format validation
- Security requirement verification
- Performance requirement confirmation
- Documentation consistency checks

### Multi-Company Support
The system is designed to handle Japanese labor scenarios where workers submit attendance to multiple organizations:
- 所属企業 (main employer) - 15th of month deadline
- 作業現場 (work site) - month-end deadline, Redmine/Excel formats
- 仲介会社 (intermediary company) - month-end deadline, web input

## Next Steps for Implementation

When ready to begin implementation:

1. Choose technology stack (Node.js/TypeScript, Java/Spring, Python/Django, etc.)
2. Set up project structure following the layered architecture
3. Implement core data models based on specifications in `doc/kairos_要件整理.md`
4. Start with foundational features: User management, Authentication, Basic CRUD operations
5. Implement business logic libraries: distance calculation, work determination algorithms
6. Add advanced features: automatic attendance generation, multi-format exports

## Support and Documentation

- **Project Requirements**: See `doc/kairos_要件整理.md` for complete functional specifications
- **Terminology**: Reference `doc/kairosプロジェクト_用語集.md` for consistent term usage
- **Design Quality**: Use prompts in `doc/guideline/設計品質確保プロンプト戦略.md` for quality assurance
- **Architecture Details**: Full component breakdown in `doc/design/コンポーネント分割と責務明確化設計書.md`