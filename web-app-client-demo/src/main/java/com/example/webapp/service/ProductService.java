package com.example.webapp.service;

import com.example.webapp.model.Product;
import com.example.webapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsBySellerUsername(String username) {
        return productRepository.findBySellerUsername(username);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // VULNERABLE: Real Boolean-based Blind SQL Injection for demonstration


    public String searchProducts(String keyword)
    {
        try
        {
            if (keyword != null && !keyword.trim().isEmpty())
            {
                String sql = "SELECT COUNT(*) FROM products WHERE name LIKE '%" + keyword + "%'";
                int count = jdbcTemplate.queryForObject(sql, Integer.class);
                return count > 0 ? "Products found" : "No products found";
            }
            return "Please provide a keyword";
        }
        catch (Exception e) {return "Search error: " + e.getMessage();}
    }




/*

    public String searchProducts(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);
            return products.isEmpty() ? "No products found" : "Products found";
        }
        return "Please provide a keyword";
    }
*/
}
