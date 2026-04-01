package com.example.doanck.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.doanck.model.Combo;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {
    List<Combo> findByActiveTrue();
    List<Combo> findByNameContainingIgnoreCase(String name);
}