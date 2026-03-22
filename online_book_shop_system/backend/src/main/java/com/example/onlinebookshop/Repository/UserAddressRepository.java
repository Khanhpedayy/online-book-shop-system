package com.example.onlinebookshop.Repository;

import com.example.onlinebookshop.Entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUser_IdAndDeletedAtIsNullOrderByDefaultAddressDescIdAsc(Long userId);

    Optional<UserAddress> findByIdAndUser_IdAndDeletedAtIsNull(Long id, Long userId);

    List<UserAddress> findByUser_IdAndDeletedAtIsNull(Long userId);
}
