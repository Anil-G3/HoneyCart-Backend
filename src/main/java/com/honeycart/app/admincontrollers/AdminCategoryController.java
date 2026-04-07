package com.honeycart.app.admincontrollers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.honeycart.app.adminservices.AdminCategoryServiceContract;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryServiceContract adminCategoryService;

    public AdminCategoryController(AdminCategoryServiceContract adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@RequestBody Map<String, Object> request) {
        try {
            String categoryName = (String) request.get("categoryName");
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminCategoryService.addCategory(categoryName));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong");
        }
    }
}