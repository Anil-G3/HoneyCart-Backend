package com.honeycart.app.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.honeycart.app.entities.Role;
import com.honeycart.app.entities.User;
import com.honeycart.app.repositories.UserRepository;
import com.honeycart.app.services.AuthServiceContract;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
@Component
public class AuthenticationFilter implements Filter {
		
		private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
		
		private final AuthServiceContract authService;
		private final UserRepository userRepository;
		
		private static final String ALLOWED_ORIGIN = "http://localhost:5173";
		
		private final String[] UNAUTHENTICATED_PATHS = {
			"/api/users/register",
			"/api/auth/login"
		};

		public AuthenticationFilter(AuthServiceContract authService, UserRepository userRepository) {
			logger.info("AuthenticationFilter initialized.");
			this.authService = authService;
			this.userRepository = userRepository;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

			try {
				executeFilterLogic(request, response, chain);
			} catch (Exception e) {
				logger.error("Unexpected error in AuthenticationFilter", e);
				sendErrorResponse((HttpServletResponse) response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
			}
			
		}

		public void executeFilterLogic(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			
			setCORSHeader(httpResponse);
			
			String requestURI = httpRequest.getRequestURI();
			logger.info("Request URI : {}", requestURI);
			
			// Handle pre-flight (OPTIONS)
			if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
				httpResponse.setStatus(HttpServletResponse.SC_OK);
				return;
			}
			
			// Allow unauthenticated parts
			if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
				chain.doFilter(request, response);
				return;
			}
			
			// Extract and validate the token
			String token = getAuthTokenFromCookies(httpRequest);
			if (token == null || !authService.validateToken(token)) {
				sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized : Invalid or missing token");
				return;
			}
			
			// Extract user name and verify user
			String username = authService.extractUsername(token);
			Optional<User> userOptional = userRepository.findByUsername(username);
			if (userOptional.isEmpty()) {
				sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: User not found");
				return;
			}
			
			// Get authenticated user and role
			User authenticatedUser = userOptional.get();
			Role role = authenticatedUser.getRole();
			logger.info("Authenticated User: {}, Role: {}", authenticatedUser.getUsername(), role);
			
			// Role based access control
			if (requestURI.startsWith("/admin/") && role != Role.ADMIN) {
				sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Admin access required");
				return;
			}
			
			if (requestURI.startsWith("/api/") && role != Role.CUSTOMER && role != Role.ADMIN) {
				sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Customer access required");
				return;
			}
			
			// Attach user details to request
			httpRequest.setAttribute("authenticatedUser", authenticatedUser);
			chain.doFilter(request, response);
			
		}
		
		private void setCORSHeader(HttpServletResponse response) {
			response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
			response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
			response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
			// FIXED: Changed "Allow-Control-Allow-Credentials" to "Access-Control-Allow-Credentials"
			response.setHeader("Access-Control-Allow-Credentials", "true");
		}
		
		private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
			response.setStatus(statusCode);
			response.getWriter().write(message);
		}
		
		private String getAuthTokenFromCookies(HttpServletRequest request) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				return Arrays.stream(cookies)
						.filter(cookie -> "authToken".equals(cookie.getName()))
						.map(Cookie::getValue)
						.findFirst()
						.orElse(null);
			}
			return null;
		}
}