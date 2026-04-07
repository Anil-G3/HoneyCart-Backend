package com.honeycart.app.controllers;

import java.util.HashMap;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.honeycart.app.dto.LoginRequest;
import com.honeycart.app.entities.User;
import com.honeycart.app.repositories.UserRepository;
import com.honeycart.app.services.AuthServiceContract;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth") 
public class AuthController {

	private final AuthServiceContract authService;
	private final UserRepository userRepository;

	public AuthController(AuthServiceContract authService, UserRepository userRepository) {
		super();
		this.authService = authService;
		this.userRepository = userRepository;
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
		try {
			
			User user = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
			
			String token = authService.generateToken(user);
			
			Cookie cookie = new Cookie("authToken", token);
			cookie.setHttpOnly(true);
			cookie.setSecure(false);
			cookie.setPath("/");
			cookie.setMaxAge(3600);
			cookie.setDomain("localhost");
			response.addCookie(cookie);
			
			//Optional but useful
			response.addHeader("Set-Cookie", String.format("authToken=%s; HttpOnly; Path=/; Max-Age=3600; SameSite=None", token));
			
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("message", "Login successful");
			responseBody.put("role", user.getRole().name());
			responseBody.put("username", user.getUsername());
			
			return ResponseEntity.ok(responseBody);
			
			
		} catch (RuntimeException e) {
		
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
			
		}
		
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
		try {
			User user = (User) request.getAttribute("authenticatedUser");
			authService.logout(user);
			Cookie cookie = new Cookie("authToken", null);
			cookie.setHttpOnly(true);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
			Map<String, String> responseBody = new HashMap<>();
			responseBody.put("message", "Logout successful");
			return ResponseEntity.ok(responseBody);
		} catch (RuntimeException e) {
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("message", "Logout failed");
			return ResponseEntity.status(500).body(errorResponse);
		}
	}
	
	@GetMapping("/session")
	public ResponseEntity<?> getSession(HttpServletRequest request) {
	    try {
	        Cookie[] cookies = request.getCookies();
	        if (cookies == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
	        }

	        String token = null;
	        for (Cookie cookie : cookies) {
	            if (cookie.getName().equals("authToken")) {
	                token = cookie.getValue();
	                break;
	            }
	        }

	        if (token == null || !authService.validateToken(token)) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
	        }

	        String username = authService.extractUsername(token);
	        String role = authService.extractRole(token);

	        User user = userRepository.findByUsername(username)
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        Map<String, Object> responseBody = new HashMap<>();
	        responseBody.put("username", username);
	        responseBody.put("role", role);
	        responseBody.put("email", user.getEmail());

	        return ResponseEntity.ok(responseBody);

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
	    }
	}
}
