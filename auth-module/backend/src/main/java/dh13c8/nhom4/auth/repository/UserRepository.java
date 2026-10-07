package dh13c8.nhom4.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dh13c8.nhom4.auth.entity.Role;
import dh13c8.nhom4.auth.entity.User;

/**
 * Repository cho User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Tìm users chưa bị xóa mềm
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllActive();

    // Tìm user theo ID và chưa bị xóa mềm
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdAndNotDeleted(@Param("id") Long id);

    // Tìm user theo username và chưa bị xóa mềm
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsernameAndNotDeleted(@Param("username") String username);

    // Tìm kiếm theo username hoặc email (chưa bị xóa mềm)
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchByUsernameOrEmail(@Param("search") String search);

    // Lọc theo role (chưa bị xóa mềm)
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.role = :role")
    List<User> findByRole(@Param("role") Role role);

    // Lọc theo trạng thái is_active (chưa bị xóa mềm)
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.isActive = :isActive")
    List<User> findByIsActive(@Param("isActive") Boolean isActive);

    // Tìm kiếm và lọc kết hợp (chưa bị xóa mềm)
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:isActive IS NULL OR u.isActive = :isActive)")
    List<User> findByFilters(@Param("search") String search,
                             @Param("role") Role role,
                             @Param("isActive") Boolean isActive);
}

