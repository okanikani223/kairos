package com.github.okanikani.kairos.users.others.repositories;

import com.github.okanikani.kairos.users.domains.models.entities.User;
import com.github.okanikani.kairos.users.domains.models.repositories.UserRepository;
import com.github.okanikani.kairos.users.others.jpa.entities.UserJpaEntity;
import com.github.okanikani.kairos.users.others.jpa.repositories.UserJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JPAユーザーリポジトリ実装
 * PostgreSQLデータベースを使用した永続化
 */
@Repository
@ConditionalOnProperty(name = "kairos.repositories.type", havingValue = "jpa")
public class JpaUserRepository implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;
    
    public JpaUserRepository(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = Objects.requireNonNull(userJpaRepository, "userJpaRepositoryは必須です");
    }
    
    @Override
    @Transactional
    public User save(User user) {
        Objects.requireNonNull(user, "ユーザーは必須です");
        
        UserJpaEntity entity = UserJpaEntity.fromDomain(user);
        UserJpaEntity savedEntity = userJpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        Objects.requireNonNull(id, "IDは必須です");
        
        return userJpaRepository.findById(id)
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUserId(String userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        
        return userJpaRepository.findByUserId(userId)
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "メールアドレスは必須です");
        
        return userJpaRepository.findByEmailIgnoreCase(email)
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
            .map(UserJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findByEnabledTrue() {
        return userJpaRepository.findByEnabledTrue().stream()
            .map(UserJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(String userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        
        return userJpaRepository.existsByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "メールアドレスは必須です");
        
        return userJpaRepository.existsByEmailIgnoreCase(email);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "IDは必須です");
        
        userJpaRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        Objects.requireNonNull(userId, "ユーザーIDは必須です");
        
        userJpaRepository.deleteByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, "IDは必須です");
        
        return userJpaRepository.existsById(id);
    }
}