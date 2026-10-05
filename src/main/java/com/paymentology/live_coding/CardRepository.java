package com.paymentology.live_coding;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, String> {

    Card findByCardNumber(String cardNumber);

}
