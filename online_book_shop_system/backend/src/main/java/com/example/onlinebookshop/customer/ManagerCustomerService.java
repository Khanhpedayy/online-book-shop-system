package com.example.onlinebookshop.customer;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ManagerCustomerService {
    private final ManagerCustomerRepository repo;

    public ManagerCustomerService(ManagerCustomerRepository repo) {
        this.repo = repo;
    }

    public List<CustomerDTO> search(String query) {
        return repo.search(query);
    }
}

