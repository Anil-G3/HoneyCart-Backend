package com.honeycart.app.services;

import java.util.List;
import java.util.Map;

import com.honeycart.app.entities.Product;

public interface ProductServiceContract {

	public List<Product> getProductsByCategory(String categoryName);
	public Map<Integer, List<String>> getImagesForProductIds(List<Integer> productIds);
	public List<Product> getCategoryPreviews(List<String> categoryNames);

}