package com.honeycart.app.adminservices;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.honeycart.app.entities.Order;


public interface AdminBusinessServiceContract {

    public Map<String, Object> calculateMonthlyBusiness(int month, int year);
    public Map<String, Object> calculateDailyBusiness(LocalDate date);
    public Map<String, Object> calculateYearlyBusiness(int year);
    public Map<String, Object> calculateOverallBusiness();
    public Map<String, Object> calculateBusinessMetrics(List<Order> orders);
	
}
