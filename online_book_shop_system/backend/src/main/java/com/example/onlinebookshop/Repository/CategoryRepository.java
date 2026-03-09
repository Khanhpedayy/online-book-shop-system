package com.example.onlinebookshop.Repository;


import com.example.onlinebookshop.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
