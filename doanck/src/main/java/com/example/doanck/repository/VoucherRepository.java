package com.example.doanck.repository;

import com.example.doanck.model.Voucher;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCodeIgnoreCase(String code);

    List<Voucher> findByUserUsernameOrderByCreatedAtDesc(String username);
}
