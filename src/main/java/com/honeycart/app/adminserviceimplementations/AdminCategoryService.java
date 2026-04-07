package com.honeycart.app.adminserviceimplementations;

import org.springframework.stereotype.Service;
import com.honeycart.app.adminservices.AdminCategoryServiceContract;
import com.honeycart.app.entities.Category;
import com.honeycart.app.repositories.CategoryRepository;

@Service
public class AdminCategoryService implements AdminCategoryServiceContract {

    private final CategoryRepository categoryRepository;

    public AdminCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category addCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (categoryRepository.findByCategoryName(categoryName.trim()).isPresent()) {
            throw new IllegalArgumentException(
                "Category '" + categoryName.trim() + "' already exists"
            );
        }
        return categoryRepository.save(new Category(categoryName.trim()));
    }
}