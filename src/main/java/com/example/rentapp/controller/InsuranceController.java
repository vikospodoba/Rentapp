package com.example.rentapp.controller;

import com.example.rentapp.entity.Insurance;
import com.example.rentapp.service.CarService;
import com.example.rentapp.service.InsuranceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/insurance")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class InsuranceController {

    private final InsuranceService insuranceService;
    private final CarService carService;

    public InsuranceController(InsuranceService insuranceService,
                               CarService carService) {
        this.insuranceService = insuranceService;
        this.carService = carService;
    }

    @GetMapping
    public String listAll(Model model,
                          @RequestParam(required = false) Long carId) {
        model.addAttribute("pageTitle", "Страховки");
        if (carId != null) {
            model.addAttribute("insurances", insuranceService.findByCarId(carId));
            model.addAttribute("selectedCarId", carId);
        } else {
            model.addAttribute("insurances", insuranceService.findAll());
        }
        model.addAttribute("cars", carService.findAll());
        return "insurance/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam Long carId, Model model) {
        Insurance insurance = new Insurance();
        insurance.setCar(carService.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден")));

        insurance.setStartDate(InsuranceService.getDefaultStartDate());
        insurance.setEndDate(InsuranceService.getDefaultEndDate());
        insurance.setPremiumAmount(new java.math.BigDecimal("50000.00"));

        model.addAttribute("pageTitle", "Добавить страховку");
        model.addAttribute("insurance", insurance);
        return "insurance/form";
    }

    @GetMapping("/template")
    public String showTemplateForm(@RequestParam Long carId, Model model) {
        Insurance insurance = insuranceService.buildTemplateForCar(carId);
        model.addAttribute("pageTitle", "Шаблон страховки");
        model.addAttribute("insurance", insurance);
        model.addAttribute("templateMode", true);
        return "insurance/form";
    }

    @PostMapping("/generate-all")
    public String generateForAllCars(RedirectAttributes redirectAttributes) {
        int created = insuranceService.createDefaultsForCarsWithoutInsurance();
        if (created > 0) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Создано страховок: " + created);
        } else {
            redirectAttributes.addFlashAttribute("successMessage",
                    "У всех автомобилей уже есть страховки");
        }
        return "redirect:/insurance";
    }

    @PostMapping
    public String create(@RequestParam Long carId,
                         @Valid @ModelAttribute("insurance") Insurance insurance,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Добавить страховку");
            return "insurance/form";
        }

        insuranceService.createForCar(carId, insurance);
        redirectAttributes.addFlashAttribute("successMessage", "Страховка успешно добавлена");
        return "redirect:/insurance";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Insurance insurance = insuranceService.findByIdOrThrow(id);
        model.addAttribute("pageTitle", "Редактировать страховку");
        model.addAttribute("insurance", insurance);
        return "insurance/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("insurance") Insurance insurance,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Редактировать страховку");
            return "insurance/form";
        }


        Insurance existing = insuranceService.findByIdOrThrow(id);
        insurance.setId(id);
        if (insurance.getCar() == null || insurance.getCar().getId() == null) {
            insurance.setCar(existing.getCar());
        }

        insuranceService.save(insurance);
        redirectAttributes.addFlashAttribute("successMessage", "Страховка успешно обновлена");
        return "redirect:/insurance";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        insuranceService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Страховка удалена");
        return "redirect:/insurance";
    }
}






