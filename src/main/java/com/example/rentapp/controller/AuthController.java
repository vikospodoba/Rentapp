package com.example.rentapp.controller;

import com.example.rentapp.dto.UserRegistrationDto;
import com.example.rentapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("pageTitle", "Регистрация");
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Регистрация");
            return "auth/register";
        }

        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация прошла успешно! Войдите в систему.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Регистрация");
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("pageTitle", "Вход");
        return "auth/login";
    }
}