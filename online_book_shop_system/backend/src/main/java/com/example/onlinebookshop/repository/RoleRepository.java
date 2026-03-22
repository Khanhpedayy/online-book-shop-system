package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByCode(String code);
}
