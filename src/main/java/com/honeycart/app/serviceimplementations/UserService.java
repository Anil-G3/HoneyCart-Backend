package com.honeycart.app.serviceimplementations;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.honeycart.app.entities.User;
import com.honeycart.app.repositories.UserRepository;
import com.honeycart.app.services.UserServiceContract;

@Service
public class UserService implements UserServiceContract {
	
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	@Override
	public User registerUser(User user) {
		
		// check if the user name and email already exists
		if(userRepository.findByUsername(user.getUsername()).isPresent()) {
			throw new RuntimeException("Username is already taken");
		}
		
		if(userRepository.findByEmail(user.getEmail()).isPresent()) {
			throw new RuntimeException("Email is already taken");
		}
		
		// Encode the password before saving
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		// save the user
		return userRepository.save(user);
		
	}

}
