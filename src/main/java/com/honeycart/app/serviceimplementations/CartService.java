package com.honeycart.app.serviceimplementations;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
			// Fetch the cart items for the user with product details
			List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(authenticatedUser.getUserId());

			// Create a response map to hold the cart details
			Map<String, Object> response = new HashMap<>();

			response.put("username", authenticatedUser.getUserId());
			response.put("role", authenticatedUser.getRole().toString());

			// List to hold the product details
			List<Map<String, Object>> products = new ArrayList<>();
			int overallTotalPrice = 0;

			for (CartItem cartItem : cartItems) {
				Map<String, Object> productDetails = new HashMap<>();

				// Get product details
				Product product = cartItem.getProduct();

				// Fetch product images from the ProductImageRepository
				List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(product.getProductId());
				String imageUrl = (productImages != null && !productImages.isEmpty()) ? productImages.get(0).getImageUrl() : "default-image-url";

				// Populate product details into the map
				productDetails.put("product_id", product.getProductId());
				productDetails.put("image_url", imageUrl);
				productDetails.put("name", product.getName());
				productDetails.put("description", product.getDescription());
				productDetails.put("price_per_unit", product.getPrice());
				productDetails.put("quantity", cartItem.getQuantity());
				productDetails.put("total_price", cartItem.getQuantity() * product.getPrice().doubleValue());

				// Add the product details to the products list
				products.add(productDetails);

				// Add to the overall total price
				overallTotalPrice += cartItem.getQuantity() * product.getPrice().doubleValue();
			}

			// Prepare the final cart response
			Map<String, Object> cart = new HashMap<>();
			cart.put("products", products);
			cart.put("overall_total_price", overallTotalPrice);

			// Add the cart details to the response
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
