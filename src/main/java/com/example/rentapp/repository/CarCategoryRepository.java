package com.example.rentapp.repository;

import com.example.rentapp.entity.CarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarCategoryRepository extends JpaRepository<CarCategory, Long> {

    @Query("SELECT c FROM CarCategory c ORDER BY c.name")
    List<CarCategory> findAllOrderedByName();

    CarCategory findByName(String name);
}