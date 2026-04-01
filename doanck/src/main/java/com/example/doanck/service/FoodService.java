package com.example.doanck.service;

import com.example.doanck.model.*;
import com.example.doanck.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FoodService {

    @Autowired private FoodRepository foodRepository;
    @Autowired private CategoryRepository categoryRepository;

    // ===== GET ALL =====
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    public List<Food> getActiveFoods() {
        return foodRepository.findByActiveTrue();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ===== CATEGORY =====
    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // ===== FOOD =====
    public Food getFoodById(Long id) {
        return foodRepository.findById(id).orElse(null);
    }

    public void saveFood(Food food) {

        // Preserve existing image/category data when the form omits them on update.
        if (food.getId() != null) {
            Optional<Food> oldFoodOpt = foodRepository.findById(food.getId());

            if (oldFoodOpt.isPresent()) {
                Food oldFood = oldFoodOpt.get();

                if (food.getImage() == null || food.getImage().isEmpty()) {
                    food.setImage(oldFood.getImage());
                }

                if (food.getCategory() == null) {
                    food.setCategory(oldFood.getCategory());
                }
            }
        }

        foodRepository.save(food);
    }

    public void deleteFood(Long id) {
        foodRepository.deleteById(id);
    }
}
