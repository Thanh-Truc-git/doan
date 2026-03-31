package com.example.doanck.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Combo;
import com.example.doanck.repository.ComboRepository;

@Service
public class ComboService {

    @Autowired
    private ComboRepository comboRepository;

    public List<Combo> getAllCombos() {
        return comboRepository.findAll();
    }

    public List<Combo> getActiveCombos() {
        return comboRepository.findByActiveTrue();
    }

    public Optional<Combo> getComboById(Long id) {
        return comboRepository.findById(id);
    }

    public List<Combo> searchCombos(String keyword) {
        return comboRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Combo createCombo(Combo combo) {
        normalizeCombo(combo);
        return comboRepository.save(combo);
    }

    public Combo updateCombo(Long id, Combo comboDetails) {
        Optional<Combo> combo = comboRepository.findById(id);
        if (combo.isPresent()) {
            Combo c = combo.get();
            c.setName(comboDetails.getName());
            c.setDescription(comboDetails.getDescription());
            c.setOriginalPrice(comboDetails.getOriginalPrice());
            c.setDiscountedPrice(comboDetails.getDiscountedPrice());
            c.setImage(comboDetails.getImage());
            c.setQuantity(comboDetails.getQuantity());
            c.setActive(comboDetails.isActive());

            normalizeCombo(c);
            
            return comboRepository.save(c);
        }
        return null;
    }

    private void normalizeCombo(Combo combo) {
        if (combo.getOriginalPrice() < 0) {
            combo.setOriginalPrice(0);
        }

        if (combo.getDiscountedPrice() < 0) {
            combo.setDiscountedPrice(0);
        }

        if (combo.getDiscountedPrice() > combo.getOriginalPrice()) {
            combo.setDiscountedPrice(combo.getOriginalPrice());
        }

        if (combo.getQuantity() < 0) {
            combo.setQuantity(0);
        }

        combo.setDiscountPercent(calculateDiscountPercent(
                combo.getOriginalPrice(),
                combo.getDiscountedPrice()));
    }

    private double calculateDiscountPercent(double originalPrice, double discountedPrice) {
        if (originalPrice <= 0) {
            return 0;
        }

        return ((originalPrice - discountedPrice) / originalPrice) * 100;
    }

    public void deleteCombo(Long id) {
        comboRepository.deleteById(id);
    }

    public void activateCombo(Long id) {
        Optional<Combo> combo = comboRepository.findById(id);
        if (combo.isPresent()) {
            Combo c = combo.get();
            c.setActive(true);
            comboRepository.save(c);
        }
    }

    public void deactivateCombo(Long id) {
        Optional<Combo> combo = comboRepository.findById(id);
        if (combo.isPresent()) {
            Combo c = combo.get();
            c.setActive(false);
            comboRepository.save(c);
        }
    }
}
