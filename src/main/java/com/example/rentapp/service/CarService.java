package com.example.rentapp.service;

import com.example.rentapp.entity.Car;
import com.example.rentapp.entity.CarCategory;
import com.example.rentapp.repository.CarCategoryRepository;
import com.example.rentapp.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final CarCategoryRepository carCategoryRepository;

    public CarService(CarRepository carRepository,
                      CarCategoryRepository carCategoryRepository) {
        this.carRepository = carRepository;
        this.carCategoryRepository = carCategoryRepository;
    }

    public List<Car> findAll() {
        return carRepository.findAll();
    }

    public List<Car> findAll(String sortBy, String direction) {
        return sortCars(carRepository.findAll(), sortBy, direction);
    }

    public List<Car> sortCars(List<Car> cars, String sortBy, String direction) {
        if (sortBy == null || sortBy.isBlank()) {
            return cars;
        }

        Comparator<Car> comparator = getComparator(sortBy);
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return cars.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Comparator<Car> getComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "brand" -> Comparator.comparing(Car::getBrand, String.CASE_INSENSITIVE_ORDER);
            case "price" -> Comparator.comparing(Car::getPricePerDay);
            case "year" -> Comparator.comparing(Car::getYear);
            case "model" -> Comparator.comparing(Car::getModel, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Car::getId);
        };
    }

    public List<Car> findAvailableCars() {
        return carRepository.findByStatus("AVAILABLE");
    }

    public List<Car> searchCars(String brand, String model, Double minPrice, Double maxPrice) {
        List<Car> allCars = carRepository.findAll();

        return allCars.stream()
                .filter(car -> brand == null || car.getBrand().toLowerCase().contains(brand.toLowerCase()))
                .filter(car -> model == null || car.getModel().toLowerCase().contains(model.toLowerCase()))
                .filter(car -> minPrice == null || car.getPricePerDay().doubleValue() >= minPrice)
                .filter(car -> maxPrice == null || car.getPricePerDay().doubleValue() <= maxPrice)
                .collect(Collectors.toList());
    }

    public Optional<Car> findById(Long id) {
        return carRepository.findById(id);
    }

    public Car save(Car car) {
        attachCategory(car);
        return carRepository.save(car);
    }

    private void attachCategory(Car car) {
        if (car.getCategory() != null && car.getCategory().getId() != null) {
            carCategoryRepository.findById(car.getCategory().getId())
                    .ifPresentOrElse(car::setCategory, () -> car.setCategory(null));
        } else {
            car.setCategory(null);
        }
    }

    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }

    public void updateCarStatus(Long carId, String status) {
        carRepository.findById(carId).ifPresent(car -> {
            car.setStatus(status);
            carRepository.save(car);
        });
    }

    public int makeAllAvailable() {
        List<Car> cars = carRepository.findAll();
        int updated = 0;
        for (Car car : cars) {
            if (!"AVAILABLE".equalsIgnoreCase(car.getStatus())) {
                car.setStatus("AVAILABLE");
                carRepository.save(car);
                updated++;
            }
        }
        return updated;
    }

    public long getTotalCars() {
        return carRepository.count();
    }

    public long getAvailableCarsCount() {
        return carRepository.findByStatus("AVAILABLE").size();
    }

    public List<CarCategory> getAllCategories() {
        List<CarCategory> categories = carCategoryRepository.findAllOrderedByName();
        return categories.isEmpty() ? carCategoryRepository.findAll() : categories;
    }
}
