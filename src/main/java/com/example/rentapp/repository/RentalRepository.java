package com.example.rentapp.repository;


import com.example.rentapp.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserId(Long userId);
    List<Rental> findByCarId(Long carId);
    List<Rental> findByStatusIgnoreCase(String status);
    List<Rental> findByUserIdAndStatusIgnoreCase(Long userId, String status);
}