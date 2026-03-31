package com.example.doanck.repository;

import com.example.doanck.model.PendingTicketOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingTicketOrderRepository extends JpaRepository<PendingTicketOrder, Long> {

    Optional<PendingTicketOrder> findByTxnRef(String txnRef);

    java.util.List<PendingTicketOrder> findByUsernameAndProcessedTrueAndFulfilledFalse(String username);
}
