package com.honeycart.app.serviceimplementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.honeycart.app.entities.Category;
import com.honeycart.app.entities.Product;
import com.honeycart.app.entities.ProductImage;
import com.honeycart.app.repositories.CategoryRepository;
import com.honeycart.app.repositories.ProductImageRepository;
import com.honeycart.app.repositories.ProductRepository;
import com.honeycart.app.services.ProductServiceContract;

@Service
public class ProductService implements ProductServiceContract{
	
	private ProductRepository productRepository;
	private ProductImageRepository productImageRepository;
	private CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository,
			CategoryRepository categoryRepository) {
		super();
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
		this.categoryRepository = categoryRepository;
	}

	@Override
	public List<Product> getProductsByCategory(String categoryName) {
		
		if (categoryName != null && !categoryName.isEmpty()) {
			Optional<Category> categoryOpt = categoryRepository.findByCategoryName(categoryName);
			if (categoryOpt.isPresent()) {
				Category category = categoryOpt.get();
				return productRepository.findByCategory_CategoryId(category.getCategoryId());
			} else {
				throw new RuntimeException("Category not found");
			}
		} else {
			return productRepository.findAll();
		}

	}

	@Override
	public Map<Integer, List<String>> getImagesForProductIds(List<Integer> productIds) {
	    Map<Integer, List<String>> imagesByProduct = new HashMap<>();
	    for (ProductImage img : productImageRepository.findByProduct_ProductIdIn(productIds)) {
	        imagesByProduct
	            .computeIfAbsent(img.getProduct().getProductId(), k -> new ArrayList<>())
	            .add(img.getImageUrl());
	    }
	    return imagesByProduct;
	}
	
	@Override
	public List<Product> getCategoryPreviews(List<String> categoryNames) {
	    List<Product> previews = new ArrayList<>();
	    for (String name : categoryNames) {
	        Product p = productRepository.findFirstByCategory_CategoryNameOrderByProductIdAsc(name);
	        if (p != null) previews.add(p);
	    }
	    return previews;
	}

}
