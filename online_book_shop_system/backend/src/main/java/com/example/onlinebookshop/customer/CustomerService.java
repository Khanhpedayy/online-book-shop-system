package com.example.onlinebookshop.customer;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public List<CustomerDTO> search(String query) {
        return repo.search(query);
    }
}

