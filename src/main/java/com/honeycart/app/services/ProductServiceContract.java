package com.honeycart.app.services;

import java.util.List;

import com.honeycart.app.entities.Product;

public interface ProductServiceContract {

	public List<Product> getProductsByCategory(String categoryName);
	public List<String> getProductImages(Integer productId);
	
}
