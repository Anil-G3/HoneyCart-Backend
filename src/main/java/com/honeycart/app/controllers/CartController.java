package com.honeycart.app.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.honeycart.app.entities.User;
import com.honeycart.app.services.CartServiceContract;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {

	private CartServiceContract cartService;

	public CartController(CartServiceContract cartService) {
		super();
		this.cartService = cartService;
	}
	
	@PostMapping("/add")
	@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
	public ResponseEntity<Void> addToCart(@RequestBody Map<String, Object> request, HttpServletRequest req) {
		
		User user = (User) req.getAttribute("authenticatedUser");
		String username = (String) request.get("username");
		int productId =(int) request.get("productId");
		
		int quantity = request.containsKey("quantity") ? (int) request.get("quantity") : 1;
		
		cartService.addToCart(user, productId, quantity);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	// Fetch all cart items for the user (based on username)
	@GetMapping("/items")
	public ResponseEntity<Map<String, Object>> getCartItems(HttpServletRequest request) {
		
		// Fetch User by user name to get the userId
		User user = (User) request.getAttribute("authenticatedUser");
		
		// call the service to get the cart items for the user
		Map<String, Object> response = cartService.getCartItems(user);
		
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/update")
	public ResponseEntity<Void> updateCartItemQuantity(@RequestBody Map<String, Object> request, HttpServletRequest req) {
		
		String username = request.get("username").toString();
		int productId = Integer.parseInt(request.get("productId").toString());
		int quantity = Integer.parseInt(request.get("quantity").toString());
		
		// Fetch  the user 
		User user = (User) req.getAttribute("authenticatedUser");
		
		cartService.updateCartItemQuantity(user, productId, quantity);
		
		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
	@DeleteMapping("/delete") 
	public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String, Object> request, HttpServletRequest req) {

		int productId = (int) request.get("productId");
		
		// Fetch the user using username
		User user = (User) req.getAttribute("authenticatedUser");
		
		cartService.deleteCartItem(user.getUserId(), productId);
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	
	@GetMapping("/items/count")
	public ResponseEntity<Integer> getCartItemCount(@RequestParam String username, HttpServletRequest request) {
		
		User user = (User) request.getAttribute("authenticatedUser");
		int cartCount = cartService.getCartItemCount(user.getUserId());
		
		return ResponseEntity.ok(cartCount);
	}
	
}
