package com.example.doanck.service;

import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.model.Voucher;
import com.example.doanck.repository.VoucherRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public List<Voucher> getUserVouchers(String username) {
        if (username == null || username.isBlank()) {
            return List.of();
        }
        return voucherRepository.findByUserUsernameOrderByCreatedAtDesc(username);
    }

    public List<Voucher> getAvailableVouchers(String username) {
        LocalDateTime now = LocalDateTime.now();
        return getUserVouchers(username).stream()
                .filter(voucher -> isVoucherUsable(voucher, now))
                .toList();
    }

    public Voucher getUsableVoucher(String code, User user) {
        if (code == null || code.isBlank() || user == null) {
            return null;
        }

        Optional<Voucher> voucherOptional = voucherRepository.findByCodeIgnoreCase(code.trim());
        if (voucherOptional.isEmpty()) {
            return null;
        }

        Voucher voucher = voucherOptional.get();
        if (voucher.getUser() == null || user.getId() == null || !user.getId().equals(voucher.getUser().getId())) {
            return null;
        }

        return isVoucherUsable(voucher, LocalDateTime.now()) ? voucher : null;
    }

    public Voucher createRefundVoucher(User user, Ticket ticket, double amount) {
        Voucher voucher = new Voucher();
        voucher.setUser(user);
        voucher.setCode(generateVoucherCode());
        voucher.setOriginalAmount(amount);
        voucher.setRemainingAmount(amount);
        voucher.setStatus("ACTIVE");
        voucher.setDescription(buildRefundDescription(ticket));
        voucher.setExpiresAt(LocalDateTime.now().plusMonths(6));
        return voucherRepository.save(voucher);
    }

    public double previewDiscount(String code, User user, double totalAmount) {
        Voucher voucher = getUsableVoucher(code, user);
        if (voucher == null || totalAmount <= 0) {
            return 0;
        }
        return Math.min(voucher.getRemainingAmount(), totalAmount);
    }

    public Voucher redeemVoucher(String code, User user, double requestedAmount) {
        Voucher voucher = getUsableVoucher(code, user);
        if (voucher == null || requestedAmount <= 0) {
            return null;
        }

        double usableAmount = Math.min(voucher.getRemainingAmount(), requestedAmount);
        voucher.setRemainingAmount(Math.max(0, voucher.getRemainingAmount() - usableAmount));
        voucher.setLastUsedAt(LocalDateTime.now());
        voucher.setStatus(voucher.getRemainingAmount() > 0 ? "PARTIALLY_USED" : "USED");
        return voucherRepository.save(voucher);
    }

    private boolean isVoucherUsable(Voucher voucher, LocalDateTime now) {
        if (voucher == null || voucher.getRemainingAmount() <= 0) {
            return false;
        }

        if (voucher.getStatus() != null) {
            String normalizedStatus = voucher.getStatus().trim().toUpperCase(Locale.ROOT);
            if ("USED".equals(normalizedStatus) || "EXPIRED".equals(normalizedStatus)) {
                return false;
            }
        }

        return voucher.getExpiresAt() == null || voucher.getExpiresAt().isAfter(now);
    }

    private String buildRefundDescription(Ticket ticket) {
        if (ticket == null || ticket.getShowtime() == null || ticket.getShowtime().getMovie() == null) {
            return "Hoan tien huy ve phim";
        }
        return "Hoan tien ve " + ticket.getShowtime().getMovie().getTitle() + " - ghe " + ticket.getSeatNumber();
    }

    private String generateVoucherCode() {
        return "VCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
