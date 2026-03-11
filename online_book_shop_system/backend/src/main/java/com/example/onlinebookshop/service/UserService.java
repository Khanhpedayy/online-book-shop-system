package com.example.onlinebookshop.service;

import com.example.onlinebookshop.dto.CreateUserRequest;
import com.example.onlinebookshop.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> searchUsers(String search, String roleCode, String status);

    UserDTO getUserById(Long id);

    UserDTO createUser(CreateUserRequest request);

    UserDTO updateUser(Long id, CreateUserRequest request);

    UserDTO toggleUserStatus(Long id);
}
