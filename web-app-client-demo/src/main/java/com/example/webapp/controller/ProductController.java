package com.example.webapp.controller;

import com.example.webapp.model.Product;
import com.example.webapp.model.User;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.service.UserService;
import com.example.webapp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products/list";
    }

    @GetMapping("/manage")
    public String manageProducts(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        boolean isSeller = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SELLER"));
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            model.addAttribute("products", productRepository.findAll());
        } else if (isSeller) {
            User seller = userService.getCurrentUser(authentication);
            if (seller == null) {
                return "redirect:/login";
            }
            model.addAttribute("products", productRepository.findBySeller(seller));
        } else {
            return "redirect:/login";
        }
        return "products/manage";
    }

    @GetMapping("/add")
    public String showAddForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SELLER"))) {
            return "redirect:/login";
        }
        
        model.addAttribute("product", new Product());
        return "products/form";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product, 
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User seller = userService.getCurrentUser(authentication);
            if (seller == null) {
                redirectAttributes.addFlashAttribute("error", "You must be logged in to add products");
                return "redirect:/login";
            }

            product.setSeller(seller);
            productRepository.save(product);
            
            redirectAttributes.addFlashAttribute("success", "Product added successfully");
            return "redirect:/products/manage";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product: " + e.getMessage());
            return "redirect:/products/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/products/manage";
        }

        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
            product.getSeller().getId().equals(currentUser.getId())) {
            model.addAttribute("product", product);
            return "products/form";
        }

        return "redirect:/products/manage";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id, 
                              @ModelAttribute Product product, 
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productRepository.findById(id).orElse(null);
            if (existingProduct == null) {
                redirectAttributes.addFlashAttribute("error", "Product not found");
                return "redirect:/products/manage";
            }

            User currentUser = userService.getCurrentUser(authentication);
            if (currentUser == null) {
                return "redirect:/login";
            }

            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                existingProduct.getSeller().getId().equals(currentUser.getId())) {
                
                product.setId(id);
                product.setSeller(existingProduct.getSeller());
                productRepository.save(product);
                
                redirectAttributes.addFlashAttribute("success", "Product updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this product");
            }
            
            return "redirect:/products/manage";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product: " + e.getMessage());
            return "redirect:/products/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, 
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(id).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Product not found");
                return "redirect:/products/manage";
            }

            User currentUser = userService.getCurrentUser(authentication);
            if (currentUser == null) {
                return "redirect:/login";
            }

            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                product.getSeller().getId().equals(currentUser.getId())) {
                
                productRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Product deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this product");
            }
            
            return "redirect:/products/manage";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
            return "redirect:/products/manage";
        }
    }
    
    @GetMapping("/search")
    @ResponseBody
    public String searchProducts(@RequestParam(required = false) String keyword) {
        return productService.searchProducts(keyword);
    }
    
    @GetMapping("/check-availability")
    @ResponseBody
    public Map<String, Object> checkProductAvailability(@RequestParam String productName,
                                                       @RequestParam(required = false) String location) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productRepository.findByNameContainingIgnoreCase(productName);
            boolean isAvailable = !products.isEmpty();
            
            response.put("available", isAvailable);
            response.put("productName", productName);
            response.put("location", location != null ? location : "default");
            response.put("message", isAvailable ? "Product is available" : "Product not found");
            
        } catch (Exception e) {
            response.put("error", "Check failed");
            response.put("available", false);
        }
        
        return response;
    }
    
    @GetMapping("/compare-prices")
    @ResponseBody
    public Map<String, Object> comparePrices(@RequestParam String productName,
                                            @RequestParam(required = false) String competitor) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (productName == null || productName.trim().isEmpty()) {
                response.put("error", "Product name is required");
                response.put("hasCompetition", false);
                return response;
            }
            
            List<Product> products = productRepository.findByNameContainingIgnoreCase(productName.trim());
            boolean hasCompetition = products.size() > 1;
            
            response.put("hasCompetition", hasCompetition);
            response.put("productName", productName.trim());
            response.put("competitor", competitor != null ? competitor.trim() : "N/A");
            response.put("message", hasCompetition ? "Competitive prices found" : "No competition found");
            
        } catch (Exception e) {
            response.put("error", "Comparison failed");
            response.put("hasCompetition", false);
        }
        
        return response;
    }
    
    @GetMapping("/recommendations")
    @ResponseBody
    public String getRecommendations(@RequestParam(required = false) String userPreference,
                                   @RequestParam(required = false) String budget,
                                   @RequestParam(required = false) String category) {
        try {
            List<Product> recommendations = new ArrayList<>();
            
            if (userPreference != null && !userPreference.trim().isEmpty()) {
                String sanitizedPreference = userPreference.trim().toLowerCase();
                if (sanitizedPreference.length() > 100) {
                    return "Error: Search term too long";
                }
                
                try {
                    Double budgetValue = null;
                    if (budget != null && !budget.trim().isEmpty()) {
                        budgetValue = Double.parseDouble(budget.trim());
                        if (budgetValue < 0) {
                            return "Error: Budget must be positive";
                        }
                    }
                    
                    final String finalPreference = sanitizedPreference;
                    final Double finalBudgetValue = budgetValue;
                    
                    List<Product> allProducts = productRepository.findAll();
                    recommendations = allProducts.stream()
                        .filter(p -> p.getName() != null && p.getDescription() != null)
                        .filter(p -> p.getName().toLowerCase().contains(finalPreference) ||
                                   p.getDescription().toLowerCase().contains(finalPreference))
                        .filter(p -> finalBudgetValue == null || 
                                   (p.getPrice() != null && p.getPrice().doubleValue() <= finalBudgetValue))
                        .limit(5)
                        .collect(java.util.stream.Collectors.toList());
                        
                } catch (NumberFormatException e) {
                    return "Error: Invalid budget format";
                }
            }
            
            StringBuilder result = new StringBuilder();
            result.append("Recommendations for: ").append(userPreference != null ? userPreference : "").append("\n");
            result.append("Budget: ").append(budget != null ? budget : "No limit").append("\n");
            result.append("Found ").append(recommendations.size()).append(" recommendations\n\n");
            
            for (Product product : recommendations) {
                result.append("- ").append(product.getName())
                      .append(" ($").append(product.getPrice()).append(")\n")
                      .append("  ").append(product.getDescription()).append("\n\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Recommendation error: " + e.getMessage();
        }
    }
}