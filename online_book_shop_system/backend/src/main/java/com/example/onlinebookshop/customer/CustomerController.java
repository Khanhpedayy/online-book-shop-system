package com.example.onlinebookshop.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff/customers")
@CrossOrigin(origins = "*")
@Tag(name = "18. Customer Lookup")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping("/search")
    @Operation(summary = "Search customer", description = "Look up customer by name, phone, or email")
    public List<CustomerDTO> search(@RequestParam String q) {
        return service.search(q);
    }
}

