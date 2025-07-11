package com.example.webapp.repository;

import com.example.webapp.model.Product;
import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Standard secure JPA methods
    List<Product> findBySeller(User seller);
    List<Product> findBySellerUsername(String username);
    List<Product> findByNameContainingIgnoreCase(String keyword);
}