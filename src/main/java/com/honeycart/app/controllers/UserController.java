package com.honeycart.app.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.honeycart.app.dto.UserDto;
import com.honeycart.app.entities.User;
import com.honeycart.app.services.UserServiceContract;

@RestController
@CrossOrigin(origins="http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/users")
public class UserController {

	private final UserServiceContract userService;

	public UserController(UserServiceContract userService) {
		super();
		this.userService = userService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		
		try 
		{
			
		User registeredUser = userService.registerUser(user);
		return ResponseEntity.ok(Map.of("message", "User registered successfully", "user", new UserDto(registeredUser.getUsername(), registeredUser.getEmail(), registeredUser.getRole().toString())));
		
		} catch (RuntimeException re) {
			return ResponseEntity.badRequest().body(Map.of("error", re.getMessage()));
		}

	}
	
}
