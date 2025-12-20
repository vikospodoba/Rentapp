package com.example.rentapp.service;

import com.example.rentapp.entity.Payment;
import com.example.rentapp.entity.Rental;
import com.example.rentapp.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalService rentalService;

    /**
     * Создаёт платёж (депозит) для аренды
     */
    public Payment createPayment(Long rentalId, BigDecimal amount, String paymentMethod) {
        Optional<Rental> rentalOpt = rentalService.findById(rentalId);
        if (rentalOpt.isPresent()) {
            Payment payment = new Payment();
            payment.setRental(rentalOpt.get());
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setStatus("COMPLETED");
            payment.setPaymentDate(LocalDateTime.now());

            return paymentRepository.save(payment);
        }
        throw new RuntimeException("Аренда не найдена");
    }
}
