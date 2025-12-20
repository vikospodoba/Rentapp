package com.example.rentapp.repository;

import com.example.rentapp.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByStatus(String status);
    List<Car> findByBrandContainingIgnoreCase(String brand);

    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE' AND " +
            "(:brand IS NULL OR LOWER(c.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:minPrice IS NULL OR c.pricePerDay >= :minPrice) AND " +
            "(:maxPrice IS NULL OR c.pricePerDay <= :maxPrice)")
    List<Car> findAvailableCars(@Param("brand") String brand,
                                @Param("minPrice") Double minPrice,
                                @Param("maxPrice") Double maxPrice);
}