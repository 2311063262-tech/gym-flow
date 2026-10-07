package com.gymflow.auth.repository;

import com.gymflow.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByDeletedAtIsNull();
    long countByRoleAndDeletedAtIsNull(User.Role role);
    List<User> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();

    @Query("select count(u) from User u where u.isActive = :active and u.deletedAt is null")
    long countActiveState(@Param("active") boolean active);
}
