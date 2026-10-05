package com.paymentology.live_coding;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * Card activity lookup for Customer Support agents. No access control in v1:
 * /support/** must only be routed from internal networks.
 */
@RestController
@RequestMapping("/support/users")
public class SupportCardActivityController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CardAuditEventRepository auditRepository;

    public SupportCardActivityController(CardAuditEventRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping("/{userId}/card-activity")
    public CardActivityPage cardActivity(@PathVariable("userId") String userId,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        Page<CardAuditEvent> events = auditRepository.findByCustomerUserIdOrderByOccurredAtDesc(userId, pageRequest);
        return new CardActivityPage(events.map(CardActivityItem::from).getContent(),
                events.getNumber(), events.getSize(), events.getTotalElements());
    }

}
