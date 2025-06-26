# Development Guidelines

## Code Comment Best Practices
Based on Stack Overflow's best practices for writing code comments (https://stackoverflow.blog/2021/12/23/best-practices-for-writing-code-comments/), this project follows these guidelines:

### 1. Comments Should Tell You "Why", Not "What"
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

### 2. Good Comments vs Bad Comments
- **Good**: Explain complex business logic, magic numbers, workarounds, TODOs
- **Bad**: Duplicate method names, obvious operations, excuse unclear code

### 3. Key Rules to Follow
1. **Don't duplicate code**: If the method name clearly states what it does, don't repeat it in comments
2. **Don't excuse unclear code**: Make code clearer instead of adding explaining comments
3. **Explain unidiomatic code**: When you must use unusual patterns, explain why
4. **Add context for magic numbers**: Always explain constants and configuration values
5. **Mark incomplete implementations**: Use TODO/FIXME for temporary solutions
6. **Document bug fixes**: Explain why specific workarounds exist
7. **Provide external references**: Link to specifications, RFCs, or business requirements

### 4. Comment Examples in This Project

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

### 5. When NOT to Comment
- Method names that clearly describe their purpose
- Simple getter/setter operations
- Obvious parameter mappings in DTOs
- Self-explanatory variable assignments
- Standard framework patterns (unless there's a specific business reason)

### 6. Documentation Standards
- Use Japanese for consistency with the codebase
- Write Javadoc for public APIs that will be used by other developers
- Focus comments on domain-specific knowledge that isn't obvious from code
- Keep comments up-to-date when code changes

## Testing Guidelines
- Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)` for unit tests
- Use `@MockitoBean` annotation has been deprecated since Spring Boot 3.4; use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito` instead
- Mock dependencies using Mockito for isolated unit testing
- Test classes should follow the naming convention: `{ClassUnderTest}Test`
- Test method names should be descriptive in Japanese: `execute_正常ケース_期待される結果`

## Test-Driven Development (TDD)
Based on Kent Beck's canonical approach to TDD (https://t-wada.hatenablog.jp/entry/canon-tdd-by-kent-beck), this project follows TDD principles:

### TDD Core Workflow
1. **テストリスト作成**: Create a list of expected behaviors to implement
2. **Red**: Write a single, specific test that fails
3. **Green**: Write just enough code to make the test pass
4. **Refactor**: Improve code structure while maintaining test success
5. **Repeat**: Continue until test list is empty

### TDD Principles
- **プログラミングワークフロー**: TDD is a programming workflow, not just a testing technique
- **インターフェース設計と実装設計の分離**: Separates interface design from implementation design
- **漸進的開発**: Focuses on incremental development and design
- **設計の創発**: Design emerges naturally through the TDD cycle

### What TDD Is NOT
- Not just about writing tests first
- Not about achieving high code coverage
- Not about skipping design
- Not about writing many tests upfront

### TDD Benefits
- **過剰設計・不足設計の防止**: Prevents over-designing or under-designing
- **設計の漸進的発見**: Allows design to emerge incrementally
- **システム動作への信頼**: Provides confidence in system behavior
- **既存機能の保護**: Ensures existing functionality remains intact while adding new features

### TDD Implementation Guidelines
- Start with a clear test list of behaviors to implement
- Write the smallest test that expresses one behavior
- Make the test pass with minimal implementation
- Refactor once tests are green
- Use Japanese for test method names to clearly express business requirements
- Focus on business value and domain logic in tests

### TDD for Existing Code Modifications
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

### Testing Spectrum (位置づけ)
1. **自動テスト (Automated Testing)**: Basic foundation
2. **開発者テスト (Developer Testing)**: Developers write tests
3. **テストファースト (Test-First Programming)**: Write tests before implementation
4. **テスト駆動開発 (TDD)**: Full TDD workflow with design feedback

## Commit Message Guidelines

Follow Conventional Commits specification (https://www.conventionalcommits.org/ja/v1.0.0/#%e4%bb%95%e6%a7%98):
- Use format: `<type>: <description>`
- Common types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
- Write descriptions in Japanese for consistency with codebase
- Example: `docs: CLAUDE.mdファイル更新 - Java/Spring Boot開発ガイド追加`