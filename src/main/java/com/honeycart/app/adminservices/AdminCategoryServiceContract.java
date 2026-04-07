package com.honeycart.app.adminservices;

import com.honeycart.app.entities.Category;

public interface AdminCategoryServiceContract {
    public Category addCategory(String categoryName);
}
