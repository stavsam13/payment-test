package com.paymentology.live_coding;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardAuditEventRepository extends JpaRepository<CardAuditEvent, String> {

    Page<CardAuditEvent> findByCustomerUserIdOrderByOccurredAtDesc(String customerUserId, Pageable pageable);

}
