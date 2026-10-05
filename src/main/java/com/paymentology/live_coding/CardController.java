package com.paymentology.live_coding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
public class CardController {


    @Autowired
    private CardRepository cardRepository;

    @GetMapping("/{cardNumber}")
    public void action(@PathVariable("cardNumber") String cardNumber, @RequestParam("action") String action, @RequestParam("userId") String userId) {
        System.out.println("Card Number: " + cardNumber + ", Action: " + action);
        if (action.equalsIgnoreCase("block")) {
            Card card = cardRepository.findByCardNumber(cardNumber);
            card.setStatus("BLOCKED");
            cardRepository.save(card);
        }
        if (action.equalsIgnoreCase("unblock")) {
            Card card = cardRepository.findByCardNumber(cardNumber);
            card.setStatus("ACTIVE");
            cardRepository.save(card);
        }

    }

}
