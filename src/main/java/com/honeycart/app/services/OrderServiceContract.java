package com.honeycart.app.services;

import java.util.Map;

import com.honeycart.app.entities.User;


public interface OrderServiceContract {

	public Map<String, Object> getOrdersForUser(User user);
	
}
