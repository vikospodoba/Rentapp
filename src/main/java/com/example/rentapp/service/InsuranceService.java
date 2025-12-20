package com.example.rentapp.service;

import com.example.rentapp.entity.Car;
import com.example.rentapp.entity.Insurance;
import com.example.rentapp.repository.CarRepository;
import com.example.rentapp.repository.InsuranceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final CarRepository carRepository;

    public InsuranceService(InsuranceRepository insuranceRepository,
                            CarRepository carRepository) {
        this.insuranceRepository = insuranceRepository;
        this.carRepository = carRepository;
    }

    public List<Insurance> findAll() {
        return insuranceRepository.findAll();
    }

    public List<Insurance> findByCarId(Long carId) {
        return insuranceRepository.findByCarId(carId);
    }

    public Insurance findByIdOrThrow(Long id) {
        return insuranceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Страховка не найдена"));
    }

    public Insurance createForCar(Long carId, Insurance insurance) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));
        insurance.setCar(car);
        return insuranceRepository.save(insurance);
    }


    public static LocalDate getDefaultStartDate() {
        return LocalDate.of(LocalDate.now().getYear(), 1, 1);
    }

    public static LocalDate getDefaultEndDate() {
        return LocalDate.of(LocalDate.now().getYear(), 12, 31);
    }

    public Insurance buildTemplateForCar(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));

        Insurance insurance = new Insurance();
        insurance.setCar(car);
        insurance.setInsuranceCompany("ООО \"Страхование Плюс\"");
        insurance.setPolicyNumber("TPL-" + car.getId() + "-" + LocalDate.now().getYear());
        insurance.setCoverageType("КАСКО + ОСАГО");
        insurance.setStartDate(getDefaultStartDate());
        insurance.setEndDate(getDefaultEndDate());
        insurance.setPremiumAmount(new BigDecimal("50000.00"));
        return insurance;
    }


    public int createDefaultsForCarsWithoutInsurance() {
        List<Car> cars = carRepository.findAll();
        AtomicInteger created = new AtomicInteger();

        cars.forEach(car -> {
            if (!insuranceRepository.existsByCarId(car.getId())) {
                Insurance insurance = buildTemplateForCar(car.getId());
                insuranceRepository.save(insurance);
                created.incrementAndGet();
            }
        });

        return created.get();
    }

    public Insurance save(Insurance insurance) {
        return insuranceRepository.save(insurance);
    }

    public void delete(Long id) {
        insuranceRepository.deleteById(id);
    }
}






