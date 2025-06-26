package com.github.okanikani.kairos.users.others.repositories;

import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * インメモリユーザーリポジトリ実装
 * 開発・テスト環境での使用を想定
 * 
 * ※これは開発・テスト用の一時的な実装です。
 * 本番環境ではデータベースを使用した実装に置き換える必要があります。
 * TODO: PostgreSQL等を使用した永続化実装への置き換え
 */
@Repository
@ConditionalOnProperty(name = "kairos.repositories.type", havingValue = "inmemory", matchIfMissing = true)
public class InMemoryUserRepository implements UserRepository {
    
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "ユーザーは必須です");
        
        if (user.id() == null) {
            // 新規ユーザーの場合、IDを自動生成
            Long newId = idGenerator.getAndIncrement();
            User userWithId = new User(
                newId,
                user.userId(),
                user.username(),
                user.email(),
                user.hashedPassword(),
                user.role(),
                user.enabled(),
                user.createdAt(),
                user.lastLoginAt()
            );
            users.put(newId, userWithId);
            return userWithId;
        } else {
            // 既存ユーザーの更新
            users.put(user.id(), user);
            return user;
        }
    }
    
    @Override
    public Optional<User> findById(Long id) {
        Objects.requireNonNull(id, "IDは必須です");
        return Optional.ofNullable(users.get(id));
    }
    
    @Override
    public Optional<User> findByUserId(String userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        return users.values().stream()
            .filter(user -> userId.equals(user.userId()))
            .findFirst();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "メールアドレスは必須です");
        return users.values().stream()
            .filter(user -> email.equalsIgnoreCase(user.email()))
            .findFirst();
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public List<User> findByEnabledTrue() {
        return users.values().stream()
            .filter(User::enabled)
            .toList();
    }
    
    @Override
    public boolean existsByUserId(String userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        return users.values().stream()
            .anyMatch(user -> userId.equals(user.userId()));
    }
    
    @Override
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "メールアドレスは必須です");
        return users.values().stream()
            .anyMatch(user -> email.equalsIgnoreCase(user.email()));
    }
    
    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "IDは必須です");
        users.remove(id);
    }
    
    @Override
    public void deleteByUserId(String userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        users.values().removeIf(user -> userId.equals(user.userId()));
    }
    
    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, "IDは必須です");
        return users.containsKey(id);
    }
    
    /**
     * 全データクリア（テスト用）
     */
    public void clear() {
        users.clear();
        idGenerator.set(1);
    }
    
    /**
     * データ件数取得（テスト用）
     * 
     * @return 保存されているユーザー数
     */
    public int size() {
        return users.size();
    }
}