package com.honeycart.app.adminserviceimplementations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.honeycart.app.entities.Category;
import com.honeycart.app.entities.Product;
import com.honeycart.app.entities.ProductImage;
import com.honeycart.app.repositories.CartRepository;
import com.honeycart.app.repositories.CategoryRepository;
import com.honeycart.app.repositories.ProductImageRepository;
import com.honeycart.app.repositories.ProductRepository;


@Service
public class AdminProductService implements com.honeycart.app.adminservices.AdminProductServiceContract {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;

    public AdminProductService(ProductRepository productRepository, ProductImageRepository productImageRepository, CategoryRepository categoryRepository, CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.cartRepository = cartRepository;  
    }

    @Override
    public Product addProductWithImage(String name, String description, Double price, Integer stock, Integer categoryId, String imageUrl) {
        
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isEmpty()) {
            throw new IllegalArgumentException("Invalid category ID");
        }

        
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStock(stock);
        product.setCategory(category.get());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(savedProduct);
            productImage.setImageUrl(imageUrl);
            productImageRepository.save(productImage);
        } else {
            throw new IllegalArgumentException("Product image URL cannot be empty");
        }

        return savedProduct;
    }

    @Override
    public void deleteProduct(Integer productId) {
       
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }

        cartRepository.deleteByProductId(productId);
       
        productImageRepository.deleteByProductId(productId);

        productRepository.deleteById(productId);
    }
    
    @Override
    public Product modifyProduct(Integer productId, String name, String description, Double price, Integer stock, Integer categoryId, String imageUrl) {
        
        Optional<Product> existingProduct = productRepository.findById(productId);
        if (existingProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        Product product = existingProduct.get();

        if (name        != null && !name.trim().isEmpty())        product.setName(name);
        if (description != null && !description.trim().isEmpty()) product.setDescription(description);
        if (price       != null && price > 0)                     product.setPrice(BigDecimal.valueOf(price));
        if (stock       != null && stock >= 0)                    product.setStock(stock);

        if (categoryId != null) {
            Optional<Category> category = categoryRepository.findById(categoryId);
            if (category.isEmpty()) {
                throw new IllegalArgumentException("Invalid category ID");
            }
            product.setCategory(category.get());
        }

        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
        	List<ProductImage> existingImages = productImageRepository.findByProduct_ProductId(productId);
        	if (!existingImages.isEmpty()) {
        	    existingImages.get(0).setImageUrl(imageUrl);
        	    productImageRepository.save(existingImages.get(0));
        	} else {
        	    ProductImage productImage = new ProductImage();
        	    productImage.setProduct(savedProduct);
        	    productImage.setImageUrl(imageUrl);
        	    productImageRepository.save(productImage);
        	}
        }
        return savedProduct;
    }
    
    @Override
    public Map<String, Object> getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<ProductImage> images = productImageRepository.findByProduct_ProductId(productId);
        String imageUrl = images.isEmpty() ? "" : images.get(0).getImageUrl();

        Map<String, Object> response = new HashMap<>();
        response.put("productId",   product.getProductId());
        response.put("name",        product.getName());
        response.put("description", product.getDescription());
        response.put("price",       product.getPrice());
        response.put("stock",       product.getStock());
        response.put("imageUrl",    imageUrl);
        response.put("category",    product.getCategory());
        return response;
    }
    
}
