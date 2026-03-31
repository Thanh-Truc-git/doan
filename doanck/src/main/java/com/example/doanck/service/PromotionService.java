package com.example.doanck.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Promotion;
import com.example.doanck.repository.PromotionRepository;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public List<Promotion> getActivePromotions() {
        return promotionRepository.findByActiveTrue();
    }

    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }

    public Optional<Promotion> getPromotionByCode(String code) {
        return promotionRepository.findByCode(code);
    }

    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public Promotion updatePromotion(Long id, Promotion promotionDetails) {
        Optional<Promotion> promotion = promotionRepository.findById(id);
        if (promotion.isPresent()) {
            Promotion p = promotion.get();
            p.setCode(promotionDetails.getCode());
            p.setDescription(promotionDetails.getDescription());
            p.setDiscountPercent(promotionDetails.getDiscountPercent());
            p.setDiscountAmount(promotionDetails.getDiscountAmount());
            p.setStartDate(promotionDetails.getStartDate());
            p.setEndDate(promotionDetails.getEndDate());
            p.setUsageLimit(promotionDetails.getUsageLimit());
            return promotionRepository.save(p);
        }
        return null;
    }

    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }

    public boolean validatePromotion(String code) {
        Optional<Promotion> promotion = getPromotionByCode(code);
        if (promotion.isPresent()) {
            return promotion.get().isValid();
        }
        return false;
    }

    public void incrementUsageCount(Long promotionId) {
        Optional<Promotion> promotion = promotionRepository.findById(promotionId);
        if (promotion.isPresent()) {
            Promotion p = promotion.get();
            p.setUsageCount(p.getUsageCount() + 1);
            promotionRepository.save(p);
        }
    }
}