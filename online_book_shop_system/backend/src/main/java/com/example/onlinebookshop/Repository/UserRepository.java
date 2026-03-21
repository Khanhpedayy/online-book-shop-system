package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

        Optional<User> findByEmail(String email);

        Optional<User> findByEmailAndDeletedAtIsNull(String email);

        Optional<User> findByFullNameIgnoreCaseAndDeletedAtIsNull(String fullName);

        Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

        @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.deletedAt IS NULL " +
                        "AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:roleCode IS NULL OR u.role.code = :roleCode) " +
                        "AND (:status IS NULL OR u.status = :status) " +
                        "ORDER BY u.createdAt DESC")
        List<User> searchUsers(@Param("search") String search,
                        @Param("roleCode") String roleCode,
                        @Param("status") String status);

        @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.id = :id AND u.deletedAt IS NULL")
        Optional<User> findByIdWithRole(@Param("id") Long id);
}
