package com.example.rentapp.repository;

import com.example.rentapp.entity.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsuranceRepository extends JpaRepository<Insurance, Long> {

    List<Insurance> findByCarId(Long carId);

    boolean existsByCarId(Long carId);
}
