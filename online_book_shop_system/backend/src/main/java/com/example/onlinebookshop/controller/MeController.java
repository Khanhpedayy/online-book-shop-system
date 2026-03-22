package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.Service.CustomerAccountService;
import com.example.onlinebookshop.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CustomerAccountService customerAccountService;

    public MeController(CustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;
    }

    @GetMapping("/profile")
    public UserDTO getProfile(Authentication auth) {
        return customerAccountService.getProfile(auth.getName());
    }

    @PutMapping("/profile")
    public UserDTO updateProfile(@RequestBody ProfileUpdateRequest request, Authentication auth) {
        return customerAccountService.updateProfile(auth.getName(), request);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody ChangePasswordRequest request, Authentication auth) {
        customerAccountService.changePassword(auth.getName(), request);
    }

    @GetMapping("/addresses")
    public List<AddressDTO> listAddresses(Authentication auth) {
        return customerAccountService.listAddresses(auth.getName());
    }

    @PostMapping("/addresses")
    public AddressDTO createAddress(@RequestBody AddressRequest request, Authentication auth) {
        return customerAccountService.createAddress(auth.getName(), request);
    }

    @PutMapping("/addresses/{id}")
    public AddressDTO updateAddress(@PathVariable Long id,
                                    @RequestBody AddressRequest request,
                                    Authentication auth) {
        return customerAccountService.updateAddress(auth.getName(), id, request);
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(@PathVariable Long id, Authentication auth) {
        customerAccountService.deleteAddress(auth.getName(), id);
    }

    @PostMapping("/addresses/{id}/default")
    public AddressDTO setDefaultAddress(@PathVariable Long id, Authentication auth) {
        return customerAccountService.setDefaultAddress(auth.getName(), id);
    }
}
