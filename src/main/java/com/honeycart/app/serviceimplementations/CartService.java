package com.honeycart.app.serviceimplementations;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.honeycart.app.entities.CartItem;
import com.honeycart.app.entities.Product;
import com.honeycart.app.entities.User;
import com.honeycart.app.repositories.CartRepository;
import com.honeycart.app.repositories.ProductImageRepository;
import com.honeycart.app.repositories.ProductRepository;
import com.honeycart.app.repositories.UserRepository;
import com.honeycart.app.services.CartServiceContract;
import com.honeycart.app.entities.ProductImage;

@Service
public class CartService implements CartServiceContract{

	private ProductRepository productRepository;
	private CartRepository cartRepository;
	private UserRepository userRepository;
	private final ProductImageRepository productImageRepository;
	
	public CartService(ProductRepository productRepository, CartRepository cartRepository, UserRepository userRepository, ProductImageRepository productImageRepository) {
		super();
		this.productRepository = productRepository;
		this.cartRepository = cartRepository;
		this.userRepository = userRepository;
		this.productImageRepository = productImageRepository;
	}

	@Override
	public void addToCart(User user, int productId, int quantity) {

		Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId)); 
		
		// Fetch cart item for this userId and productId
		Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(user.getUserId(), productId);
		
		if (existingItem.isPresent()) {
			CartItem cartItem = existingItem.get();
			cartItem.setQuantity(cartItem.getQuantity() + quantity);
			cartRepository.save(cartItem);
		} else {
			CartItem newItem = new CartItem(user, product, quantity);
			cartRepository.save(newItem);
		}
		
	}
	
	// Get Cart Items for a User
	public Map<String, Object> getCartItems(User authenticatedUser) {
	    List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(authenticatedUser.getUserId());

	    Map<String, Object> response = new HashMap<>();
	    response.put("username", authenticatedUser.getUserId());
	    response.put("role", authenticatedUser.getRole().toString());

	    // Bulk-fetch images for every product in the cart in ONE query
	    List<Integer> productIds = cartItems.stream()
	            .map(item -> item.getProduct().getProductId())
	            .collect(Collectors.toList());

	    Map<Integer, String> firstImageByProduct = new HashMap<>();
	    for (ProductImage img : productImageRepository.findByProduct_ProductIdIn(productIds)) {
	        firstImageByProduct.putIfAbsent(img.getProduct().getProductId(), img.getImageUrl());
	    }

	    List<Map<String, Object>> products = new ArrayList<>();
	    int overallTotalPrice = 0;

	    for (CartItem cartItem : cartItems) {
	        Map<String, Object> productDetails = new HashMap<>();
	        Product product = cartItem.getProduct();
	        String imageUrl = firstImageByProduct.getOrDefault(product.getProductId(), "default-image-url");

	        productDetails.put("product_id", product.getProductId());
	        productDetails.put("image_url", imageUrl);
	        productDetails.put("name", product.getName());
	        productDetails.put("description", product.getDescription());
	        productDetails.put("price_per_unit", product.getPrice());
	        productDetails.put("quantity", cartItem.getQuantity());
	        productDetails.put("total_price", cartItem.getQuantity() * product.getPrice().doubleValue());

	        products.add(productDetails);
	        overallTotalPrice += cartItem.getQuantity() * product.getPrice().doubleValue();
	    }

	    Map<String, Object> cart = new HashMap<>();
	    cart.put("products", products);
	    cart.put("overall_total_price", overallTotalPrice);

	    response.put("cart", cart);
	    return response;
	}
		
		@Override
		public void updateCartItemQuantity(User authenticatedUser, int productId, int quantity) {

			User user = userRepository.findById(authenticatedUser.getUserId()).orElseThrow(() ->  new IllegalArgumentException("User not found"));
			
			Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
			
			// Fetch cart items for this userId and productId
			Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(authenticatedUser.getUserId(), productId);
			
			if (existingItem.isPresent()) {
				CartItem cartItem = existingItem.get();
				if (cartItem.getQuantity() == 0) {
					deleteCartItem(authenticatedUser.getUserId(), productId);
				} else {
					cartItem.setQuantity(quantity);
					cartRepository.save(cartItem);
				}
			}
			
		}
		
		@Override
		public void deleteCartItem(int userId, int productId) {
		
			User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
			
			Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
			
			cartRepository.deleteCartItem(userId, productId);
			
		}

		@Override
		public int getCartItemCount(int userId) {

			int count = cartRepository.countTotalItems(userId);
			
			return count;
		}

}
