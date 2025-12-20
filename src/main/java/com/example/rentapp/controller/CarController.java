package com.example.rentapp.controller;

import com.example.rentapp.entity.Car;
import com.example.rentapp.entity.CarCategory;
import com.example.rentapp.service.CarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @ModelAttribute("categories")
    public List<CarCategory> categories() {
        return carService.getAllCategories();
    }

    @GetMapping
    public String listCars(Model model,
                           @RequestParam(required = false) String brand,
                           @RequestParam(name = "model", required = false) String modelName,
                           @RequestParam(required = false) Double minPrice,
                           @RequestParam(required = false) Double maxPrice,
                           @RequestParam(required = false) String sortBy,
                           @RequestParam(required = false) String direction) {

        var cars = carService.searchCars(brand, modelName, minPrice, maxPrice);
        cars = carService.sortCars(cars, sortBy, direction);

        model.addAttribute("pageTitle", "Каталог автомобилей");
        model.addAttribute("cars", cars);
        model.addAttribute("searchBrand", brand);
        model.addAttribute("searchModel", modelName);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        return "cars/list";
    }

    @GetMapping("/available")
    public String availableCars(Model model) {
        model.addAttribute("pageTitle", "Доступные автомобили");
        model.addAttribute("cars", carService.findAvailableCars());
        model.addAttribute("searchBrand", null);
        model.addAttribute("searchModel", null);
        return "cars/list";
    }

    @GetMapping("/{id}")
    public String viewCar(@PathVariable Long id, Model model) {
        Optional<Car> car = carService.findById(id);
        if (car.isPresent()) {
            model.addAttribute("pageTitle", car.get().getFullName());
            model.addAttribute("car", car.get());
            return "cars/view";
        }
        return "redirect:/cars";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Добавить автомобиль");
        Car car = new Car();
        model.addAttribute("car", car);
        model.addAttribute("categories", carService.getAllCategories());
        return "cars/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createCar(@Valid @ModelAttribute Car car,
                            @RequestParam(required = false) Long categoryId,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Добавить автомобиль");
            model.addAttribute("categories", carService.getAllCategories());
            return "cars/form";
        }

        if (categoryId != null && categoryId > 0) {
            CarCategory category = new CarCategory();
            category.setId(categoryId);
            car.setCategory(category);
        } else {
            car.setCategory(null);
        }

        carService.save(car);
        redirectAttributes.addFlashAttribute("successMessage", "Автомобиль успешно добавлен!");
        return "redirect:/cars";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Car> car = carService.findById(id);
        if (car.isPresent()) {
            model.addAttribute("pageTitle", "Редактировать автомобиль");
            Car editable = car.get();
            model.addAttribute("car", editable);
            model.addAttribute("categories", carService.getAllCategories());
            return "cars/form";
        }
        return "redirect:/cars";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCar(@PathVariable Long id,
                            @Valid @ModelAttribute Car car,
                            @RequestParam(required = false) Long categoryId,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Редактировать автомобиль");
            Optional<Car> existingCar = carService.findById(id);
            if (existingCar.isPresent()) {
                model.addAttribute("car", existingCar.get());
            }
            model.addAttribute("categories", carService.getAllCategories());
            return "cars/form";
        }

        if (categoryId != null && categoryId > 0) {
            CarCategory category = new CarCategory();
            category.setId(categoryId);
            car.setCategory(category);
        } else {
            car.setCategory(null);
        }

        car.setId(id);
        carService.save(car);
        redirectAttributes.addFlashAttribute("successMessage", "Автомобиль успешно обновлён!");
        return "redirect:/cars";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        carService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Автомобиль удалён!");
        return "redirect:/cars";
    }

    @PostMapping("/make-available")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String makeAllAvailable(RedirectAttributes redirectAttributes) {
        int updated = carService.makeAllAvailable();
        String message = updated > 0
                ? "Автомобили обновлены: " + updated + " теперь в статусе AVAILABLE"
                : "Все автомобили уже были доступны";
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/cars";
    }
}