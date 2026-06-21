package com.honeycart.app.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.honeycart.app.entities.Product;
import com.honeycart.app.entities.User;
import com.honeycart.app.services.ProductServiceContract;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	
	 private ProductServiceContract productService;

	public ProductController(ProductServiceContract productService) {
		super();
		this.productService = productService;
	}
	
	@GetMapping 
	public ResponseEntity<Map<String, Object>> getProducts(@RequestParam(required = false) String category, HttpServletRequest request) {

		try
		{
			User authenticatedUser = (User) request.getAttribute("authenticatedUser");
			
			if (authenticatedUser == null) {
				return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
			}
			
			List<Product> products = productService.getProductsByCategory(category);
			
			// Fetch images for ALL products in one query instead of one query per product
			List<Integer> productIds = products.stream()
					.map(Product::getProductId)
					.collect(Collectors.toList());
			Map<Integer, List<String>> imagesByProduct = productService.getImagesForProductIds(productIds);
			
			Map<String, Object> response = new HashMap<>();
			
			Map<String, String> userInfo = new HashMap<>();
			userInfo.put("name", authenticatedUser.getUsername());
			userInfo.put("role", authenticatedUser.getRole().name());
			response.put("user", userInfo);
			
			List<Map<String, Object>> productList = new ArrayList<>();
			for (Product product : products) {
				Map<String, Object> productDetails = new HashMap<>();
				productDetails.put("product_id", product.getProductId());
				productDetails.put("name", product.getName());
				productDetails.put("description", product.getDescription());
				productDetails.put("price", product.getPrice());
				productDetails.put("stock", product.getStock());
				productDetails.put("images", imagesByProduct.getOrDefault(product.getProductId(), List.of()));
				productList.add(productDetails);
			}
			
			response.put("products", productList);
			return ResponseEntity.ok(response);
			
		}
		catch (RuntimeException re) 
		{
			return ResponseEntity.badRequest().body(Map.of("error", re.getMessage()));
		}

	}
	
	@GetMapping("/preview")
	public ResponseEntity<Map<String, Object>> getCategoryPreviews(
	        @RequestParam List<String> categories, HttpServletRequest request) {

	    User authenticatedUser = (User) request.getAttribute("authenticatedUser");
	    if (authenticatedUser == null) {
	        return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
	    }

	    List<Product> products = productService.getCategoryPreviews(categories);

	    List<Integer> productIds = products.stream().map(Product::getProductId).collect(Collectors.toList());
	    Map<Integer, List<String>> imagesByProduct = productService.getImagesForProductIds(productIds);

	    List<Map<String, Object>> productList = new ArrayList<>();
	    for (Product product : products) {
	        Map<String, Object> details = new HashMap<>();
	        details.put("product_id", product.getProductId());
	        details.put("name", product.getName());
	        details.put("description", product.getDescription());
	        details.put("price", product.getPrice());
	        details.put("stock", product.getStock());
	        details.put("categoryName", product.getCategory().getCategoryName());
	        details.put("images", imagesByProduct.getOrDefault(product.getProductId(), List.of()));
	        productList.add(details);
	    }

	    return ResponseEntity.ok(Map.of("products", productList));
	}
		

}
