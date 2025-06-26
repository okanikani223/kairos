package com.github.okanikani.kairos.users.applications.usecases;

import com.github.okanikani.kairos.commons.exceptions.DuplicateResourceException;
import com.github.okanikani.kairos.commons.exceptions.ValidationException;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.RegisterUserRequest;
import com.github.okanikani.kairos.users.applications.usecases.dto.UserResponse;
import com.github.okanikani.kairos.users.applications.usecases.mapper.UserMapper;
import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.domains.models.entities.Role;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import com.github.okanikani.kairos.users.domains.services.PasswordService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * ユーザー登録ユースケース
 * 新規ユーザーの登録処理を実行
 */
@Service
public class RegisterUserUseCase {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    
    public RegisterUserUseCase(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepositoryは必須です");
        this.passwordService = Objects.requireNonNull(passwordService, "passwordServiceは必須です");
    }
    
    /**
     * ユーザー登録を実行（API用）
     * 
     * @param request 登録リクエスト（API用）
     * @return 登録されたユーザー情報
     * @throws ValidationException 入力値が不正な場合
     * @throws DuplicateResourceException ユーザーIDまたはメールアドレスが重複している場合
     */
    public UserResponse execute(RegisterRequest request) {
        Objects.requireNonNull(request, "リクエストは必須です");
        
        // 1. 入力値の基本バリデーション
        validateApiRequest(request);
        
        // 2. ユーザーIDとメールアドレスの重複チェック
        checkDuplicates(request.userId(), request.email());
        
        // 3. パスワード強度の検証
        User.validatePasswordStrength(request.password());
        
        // 4. ロールの決定（指定されていない場合はUSERを使用）
        Role role = determineRole(request.role());
        
        // 5. ユーザーエンティティの作成
        User newUser = User.createNewWithRole(
            request.userId(),
            request.username(),
            request.email(),
            request.password(),
            role,
            passwordService::hashPassword
        );
        
        // 6. ユーザーの保存
        User savedUser = userRepository.save(newUser);
        
        // 7. レスポンスに変換して返却
        return UserMapper.toUserResponse(savedUser);
    }
    
    /**
     * ユーザー登録を実行（従来版）
     * 
     * @param request 登録リクエスト（従来版）
     * @return 登録されたユーザー情報
     * @throws ValidationException 入力値が不正な場合
     * @throws DuplicateResourceException ユーザーIDまたはメールアドレスが重複している場合
     */
    public UserResponse execute(RegisterUserRequest request) {
        Objects.requireNonNull(request, "リクエストは必須です");
        
        // 1. 入力値の基本バリデーション
        validateRequest(request);
        
        // 2. ユーザーIDとメールアドレスの重複チェック
        checkDuplicates(request.userId(), request.email());
        
        // 3. パスワード強度の検証
        User.validatePasswordStrength(request.password());
        
        // 4. ユーザーエンティティの作成
        User newUser = User.createNew(
            request.userId(),
            request.username(),
            request.email(),
            request.password(),
            passwordService::hashPassword
        );
        
        // 5. ユーザーの保存
        User savedUser = userRepository.save(newUser);
        
        // 6. レスポンスに変換して返却
        return UserMapper.toUserResponse(savedUser);
    }
    
    /**
     * API用リクエストの基本バリデーション
     * 
     * @param request 検証対象のリクエスト
     * @throws ValidationException バリデーションエラーの場合
     */
    private void validateApiRequest(RegisterRequest request) {
        // 必須フィールドのnullチェック
        if (request.userId() == null || request.userId().trim().isEmpty()) {
            throw new ValidationException("ユーザーIDは必須です");
        }
        
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new ValidationException("ユーザー名は必須です");
        }
        
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new ValidationException("メールアドレスは必須です");
        }
        
        if (request.password() == null || request.password().trim().isEmpty()) {
            throw new ValidationException("パスワードは必須です");
        }
    }
    
    /**
     * 従来版リクエストの基本バリデーション
     * 
     * @param request 検証対象のリクエスト
     * @throws ValidationException バリデーションエラーの場合
     */
    private void validateRequest(RegisterUserRequest request) {
        // 必須フィールドのnullチェック
        if (request.userId() == null || request.userId().trim().isEmpty()) {
            throw new ValidationException("ユーザーIDは必須です");
        }
        
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new ValidationException("ユーザー名は必須です");
        }
        
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new ValidationException("メールアドレスは必須です");
        }
        
        if (request.password() == null || request.password().trim().isEmpty()) {
            throw new ValidationException("パスワードは必須です");
        }
        
        // パスワード確認のチェック
        if (request.confirmPassword() == null || request.confirmPassword().trim().isEmpty()) {
            throw new ValidationException("パスワード確認は必須です");
        }
        
        if (!request.password().equals(request.confirmPassword())) {
            throw new ValidationException("パスワードとパスワード確認が一致しません");
        }
    }
    
    /**
     * ロールの決定
     * 
     * @param roleString ロール文字列（null可）
     * @return 決定されたロール
     */
    private Role determineRole(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return Role.USER; // デフォルトはUSER
        }
        
        try {
            return Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("無効なロールが指定されました: " + roleString);
        }
    }
    
    /**
     * ユーザーIDとメールアドレスの重複チェック
     * 
     * @param userId ユーザーID
     * @param email メールアドレス
     * @throws DuplicateResourceException 重複している場合
     */
    private void checkDuplicates(String userId, String email) {
        // ユーザーIDの重複チェック
        if (userRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("ユーザーID「" + userId + "」は既に使用されています");
        }
        
        // メールアドレスの重複チェック
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("メールアドレス「" + email + "」は既に使用されています");
        }
    }
}