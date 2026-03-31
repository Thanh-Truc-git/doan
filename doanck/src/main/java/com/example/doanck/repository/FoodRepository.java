package com.example.doanck.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.doanck.model.Category;
import com.example.doanck.model.Food;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByActiveTrue();
    List<Food> findByCategoryAndActiveTrue(Category category);
    List<Food> findByNameContainingIgnoreCase(String name);
}