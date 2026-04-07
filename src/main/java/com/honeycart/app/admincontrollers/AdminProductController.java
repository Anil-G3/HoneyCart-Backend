package com.honeycart.app.admincontrollers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.honeycart.app.adminservices.AdminProductServiceContract;
import com.honeycart.app.entities.Product;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    private final AdminProductServiceContract adminProductService;

    public AdminProductController(AdminProductServiceContract adminProductService) {
        this.adminProductService = adminProductService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> productRequest) {
        try {
            String name = (String) productRequest.get("name");
            String description = (String) productRequest.get("description");
            Double price = Double.valueOf(String.valueOf(productRequest.get("price")));
            Integer stock = (Integer) productRequest.get("stock");
            Integer categoryId = (Integer) productRequest.get("categoryId");
            String imageUrl = (String) productRequest.get("imageUrl");

            Product addedProduct = adminProductService.addProductWithImage(name, description, price, stock, categoryId, imageUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProduct(@RequestBody Map<String, Integer> requestBody) {
        try {
            Integer productId = requestBody.get("productId");
            adminProductService.deleteProduct(productId);
            return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    @PutMapping("/modify")
    public ResponseEntity<?> modifyProduct(@RequestBody Map<String, Object> productRequest) {
        try {
            Integer productId   = (Integer) productRequest.get("productId");
            String name         = (String)  productRequest.get("name");
            String description  = (String)  productRequest.get("description");
            String imageUrl     = (String)  productRequest.get("imageUrl");

            Double price = productRequest.get("price") != null
                ? Double.valueOf(String.valueOf(productRequest.get("price"))) : null;
            Integer stock = productRequest.get("stock") != null
                ? (Integer) productRequest.get("stock") : null;
            Integer categoryId = productRequest.get("categoryId") != null
                ? (Integer) productRequest.get("categoryId") : null;

            if (productId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product ID is required");
            }

            Product modifiedProduct = adminProductService.modifyProduct(
                productId, name, description, price, stock, categoryId, imageUrl
            );
            return ResponseEntity.status(HttpStatus.OK).body(modifiedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Integer productId) {
        try {
            return ResponseEntity.ok(adminProductService.getProductById(productId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }
}