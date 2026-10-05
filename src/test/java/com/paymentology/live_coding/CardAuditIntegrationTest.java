package com.paymentology.live_coding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CardAuditIntegrationTest {

    private static final String CARD_NUMBER = "123456789";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CardAuditEventRepository auditRepository;

    @BeforeEach
    void resetState() {
        jdbcTemplate.update("DELETE FROM card_audit_events");
        jdbcTemplate.update("UPDATE cards SET status = 'ACTIVE' WHERE id = 'test_card_1'");
    }

    @Test
    void customerBlockIsAuditedAsSucceeded() throws Exception {
        cardAction(CARD_NUMBER, "block", "test_user_1", "CUSTOMER", "CARD_LOST")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("SUCCEEDED"));

        List<CardAuditEvent> events = auditRepository.findAll();
        assertThat(events).hasSize(1);
        CardAuditEvent event = events.get(0);
        assertThat(event.getCardId()).isEqualTo("test_card_1");
        assertThat(event.getCustomerUserId()).isEqualTo("test_user_1");
        assertThat(event.getAction()).isEqualTo(CardAction.BLOCK);
        assertThat(event.getOutcome()).isEqualTo(AuditOutcome.SUCCEEDED);
        assertThat(event.getReason()).isEqualTo(ActionReason.CARD_LOST);
        assertThat(event.getPreviousStatus()).isEqualTo("ACTIVE");
        assertThat(event.getNewStatus()).isEqualTo("BLOCKED");
        assertThat(event.getActorType()).isEqualTo(ActorType.CUSTOMER);
        assertThat(event.getActorId()).isEqualTo("test_user_1");
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    void repeatedBlockIsRejectedAndAudited() throws Exception {
        cardAction(CARD_NUMBER, "block", "test_user_1", "CUSTOMER", "CARD_LOST").andExpect(status().isOk());

        cardAction(CARD_NUMBER, "block", "test_user_1", "CUSTOMER", "CARD_LOST")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.failureReason").value("ALREADY_BLOCKED"));

        assertThat(auditRepository.findAll())
                .extracting(CardAuditEvent::getOutcome)
                .containsExactlyInAnyOrder(AuditOutcome.SUCCEEDED, AuditOutcome.REJECTED);
    }

    @Test
    void customerActingOnSomeoneElsesCardIsRejected() throws Exception {
        cardAction(CARD_NUMBER, "block", "someone_else", "CUSTOMER", "CARD_STOLEN")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.failureReason").value("NOT_CARD_OWNER"));

        CardAuditEvent event = auditRepository.findAll().get(0);
        assertThat(event.getOutcome()).isEqualTo(AuditOutcome.REJECTED);
        assertThat(event.getCustomerUserId()).isEqualTo("test_user_1");
        assertThat(event.getActorId()).isEqualTo("someone_else");
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM cards WHERE id = 'test_card_1'", String.class))
                .isEqualTo("ACTIVE");
    }

    @Test
    void agentCanActOnCustomersCard() throws Exception {
        cardAction(CARD_NUMBER, "block", "agent_42", "AGENT", "SUSPECTED_FRAUD").andExpect(status().isOk());

        CardAuditEvent event = auditRepository.findAll().get(0);
        assertThat(event.getOutcome()).isEqualTo(AuditOutcome.SUCCEEDED);
        assertThat(event.getActorType()).isEqualTo(ActorType.AGENT);
        assertThat(event.getActorId()).isEqualTo("agent_42");
        assertThat(event.getCustomerUserId()).isEqualTo("test_user_1");
    }

    @Test
    void unknownCardIsAuditedWithoutCardReference() throws Exception {
        cardAction("999999999", "unblock", "test_user_1", "CUSTOMER", "CARD_FOUND")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.failureReason").value("CARD_NOT_FOUND"));

        CardAuditEvent event = auditRepository.findAll().get(0);
        assertThat(event.getCardId()).isNull();
        assertThat(event.getCustomerUserId()).isEqualTo("test_user_1");
        assertThat(event.getFailureReason()).isEqualTo(FailureReason.CARD_NOT_FOUND);
    }

    @Test
    void invalidParametersAreRejectedWithoutAudit() throws Exception {
        cardAction(CARD_NUMBER, "freeze", "test_user_1", "CUSTOMER", "CARD_LOST").andExpect(status().isBadRequest());
        cardAction(CARD_NUMBER, "block", "test_user_1", "CUSTOMER", "because I said so")
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/cards/{cardNumber}", CARD_NUMBER)
                        .param("action", "block").param("userId", "test_user_1"))
                .andExpect(status().isBadRequest());

        assertThat(auditRepository.count()).isZero();
    }

    @Test
    void supportSeesActivityNewestFirstPagedAndWithoutCardData() throws Exception {
        cardAction(CARD_NUMBER, "block", "test_user_1", "CUSTOMER", "CARD_LOST").andExpect(status().isOk());
        cardAction(CARD_NUMBER, "unblock", "agent_42", "AGENT", "CARD_FOUND").andExpect(status().isOk());
        cardAction(CARD_NUMBER, "unblock", "test_user_1", "CUSTOMER", "CARD_FOUND").andExpect(status().isConflict());

        String body = mockMvc.perform(get("/support/users/{userId}/card-activity", "test_user_1")
                        .param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].action").value("UNBLOCK"))
                .andExpect(jsonPath("$.items[0].outcome").value("REJECTED"))
                .andExpect(jsonPath("$.items[0].failureReason").value("ALREADY_ACTIVE"))
                .andExpect(jsonPath("$.items[1].actorType").value("AGENT"))
                .andExpect(jsonPath("$.items[1].actorId").value("agent_42"))
                .andExpect(jsonPath("$.items[1].cardId").value("test_card_1"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain(CARD_NUMBER).doesNotContainIgnoringCase("cvv").doesNotContain("01/30");
    }

    @Test
    void supportSeesNothingForOtherCustomers() throws Exception {
        cardAction(CARD_NUMBER, "block", "test_user_1", "CUSTOMER", "CARD_LOST").andExpect(status().isOk());

        mockMvc.perform(get("/support/users/{userId}/card-activity", "test_user_2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    private ResultActions cardAction(String cardNumber, String action, String userId, String actorType,
                                     String reason) throws Exception {
        return mockMvc.perform(get("/cards/{cardNumber}", cardNumber)
                .param("action", action)
                .param("userId", userId)
                .param("actorType", actorType)
                .param("reason", reason));
    }
}
