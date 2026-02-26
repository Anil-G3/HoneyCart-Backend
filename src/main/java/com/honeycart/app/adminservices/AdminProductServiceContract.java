package com.honeycart.app.adminservices;

import com.honeycart.app.entities.Product;

public interface AdminProductServiceContract {

	public Product addProductWithImage(String name, String description, Double price, Integer stock, Integer categoryId, String imageUrl);
	public void deleteProduct(Integer productId);
	
}
