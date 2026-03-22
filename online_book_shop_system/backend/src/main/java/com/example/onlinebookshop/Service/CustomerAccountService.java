package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.dto.*;

import java.util.List;

public interface CustomerAccountService {

    UserDTO getProfile(String email);

    UserDTO updateProfile(String email, ProfileUpdateRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    List<AddressDTO> listAddresses(String email);

    AddressDTO createAddress(String email, AddressRequest request);

    AddressDTO updateAddress(String email, Long addressId, AddressRequest request);

    void deleteAddress(String email, Long addressId);

    AddressDTO setDefaultAddress(String email, Long addressId);
}
