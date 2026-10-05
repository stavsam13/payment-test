package com.paymentology.live_coding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
public class CardController {


    @Autowired
    private CardActionService cardActionService;

    @GetMapping("/{cardNumber}")
    public ResponseEntity<CardActionResult> action(@PathVariable("cardNumber") String cardNumber,
                                                   @RequestParam("action") String action,
                                                   @RequestParam("userId") String userId,
                                                   @RequestParam("actorType") ActorType actorType,
                                                   @RequestParam("reason") ActionReason reason) {
        CardAction cardAction;
        try {
            cardAction = CardAction.fromParam(action);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        System.out.println("Card action: " + cardAction + ", actorType: " + actorType);
        CardActionResult result = cardActionService.perform(cardNumber, cardAction, userId, actorType, reason);
        return ResponseEntity.status(statusFor(result)).body(result);
    }

    private HttpStatus statusFor(CardActionResult result) {
        if (result.failureReason() == null) {
            return HttpStatus.OK;
        }
        return switch (result.failureReason()) {
            case CARD_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case NOT_CARD_OWNER -> HttpStatus.FORBIDDEN;
            case ALREADY_BLOCKED, ALREADY_ACTIVE -> HttpStatus.CONFLICT;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}
