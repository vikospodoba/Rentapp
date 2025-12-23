package com.example.rentapp.config;

import com.example.rentapp.entity.*;
import com.example.rentapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Profile("!test")
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private CarRepository carRepository;
    @Autowired private CarCategoryRepository categoryRepository;
    @Autowired private RentalRepository rentalRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private InsuranceRepository insuranceRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            loadSampleData();
        }
    }

    private void loadSampleData() {
        CarCategory sedan = createCategory("Sedan", "Comfortable cars for city driving");
        CarCategory suv = createCategory("SUV", "Spacious cars for family trips");
        CarCategory luxury = createCategory("Luxury", "Premium vehicles with advanced features");
        CarCategory economy = createCategory("Economy", "Budget-friendly cars for everyday use");

        User admin = createUser("admin", "admin123", "admin@rentapp.com", "Admin", "User", "ADMIN");
        User manager = createUser("manager", "manager123", "manager@rentapp.com", "Manager", "User", "MANAGER");

        Car car1 = createCar("Toyota", "Camry", 2023, new BigDecimal("45.00"), sedan,
                "Reliable sedan with great fuel economy", "AVAILABLE");
        Car car2 = createCar("Honda", "CR-V", 2023, new BigDecimal("55.00"), suv,
                "Spacious SUV perfect for family trips", "AVAILABLE");
        Car car3 = createCar("BMW", "X5", 2023, new BigDecimal("85.00"), luxury,
                "Luxury SUV with premium features", "MAINTENANCE");
        Car car4 = createCar("Ford", "Focus", 2022, new BigDecimal("35.00"), economy,
                "Economical car for city driving", "AVAILABLE");
        Car car5 = createCar("Mercedes", "E-Class", 2023, new BigDecimal("95.00"), luxury,
                "Executive sedan with luxury features", "AVAILABLE");

        // страховые полисы для каждого авто
        createInsurance(car1, "Ингосстрах", "CAM-2024-001", "КАСКО + ОСАГО",
                LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(10), new BigDecimal("900.00"));
        createInsurance(car2, "РЕСО-Гарантия", "CRV-2024-002", "ОСАГО",
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(11), new BigDecimal("520.00"));
        createInsurance(car3, "АльфаСтрахование", "X5-2024-003", "КАСКО (премиум)",
                LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(9), new BigDecimal("1500.00"));
        createInsurance(car4, "Тинькофф Страхование", "FOC-2024-004", "ОСАГО + дополнительная защита",
                LocalDate.now().minusWeeks(2), LocalDate.now().plusYears(1), new BigDecimal("430.00"));
        createInsurance(car5, "Согласие", "ECL-2024-005", "КАСКО + ОСАГО",
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(11), new BigDecimal("1350.00"));

        System.out.println("Тестовые данные загрузилимь успешно!");
        System.out.println("Реквизиты для входа админ: admin / admin123");
        System.out.println("Реквизиты для входа менеджер: manager / manager123");
    }

    private CarCategory createCategory(String name, String description) {
        CarCategory category = new CarCategory();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private User createUser(String username, String password, String email, String firstName, String lastName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Car createCar(String brand, String model, int year, BigDecimal price, CarCategory category,
                          String description, String status) {
        Car car = new Car();
        car.setBrand(brand);
        car.setModel(model);
        car.setYear(year);
        car.setPricePerDay(price);
        car.setCategory(category);
        car.setDescription(description);
        car.setStatus(status);
        return carRepository.save(car);
    }

    private Insurance createInsurance(Car car, String company, String policy, String coverage,
                                      LocalDate start, LocalDate end, BigDecimal premium) {
        Insurance insurance = new Insurance();
        insurance.setCar(car);
        insurance.setInsuranceCompany(company);
        insurance.setPolicyNumber(policy);
        insurance.setCoverageType(coverage);
        insurance.setStartDate(start);
        insurance.setEndDate(end);
        insurance.setPremiumAmount(premium);
        return insuranceRepository.save(insurance);
    }

    private Rental createRental(User user, Car car, LocalDate startDate, LocalDate endDate, String status) {
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setStatus(status);
        rental.calculateTotalPrice();
        if ("CONFIRMED".equals(status) || "ACTIVE".equals(status)) {
            car.setStatus("RENTED");
            carRepository.save(car);
        }
        return rentalRepository.save(rental);
    }

    private Payment createPayment(Rental rental, BigDecimal amount, String paymentMethod, String status) {
        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

}
