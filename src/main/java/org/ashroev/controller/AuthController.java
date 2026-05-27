package org.ashroev.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ashroev.dto.RegistrationRequest;
import org.ashroev.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationForm") RegistrationRequest request,
            BindingResult bindingResult) {

        if (StringUtils.hasText(request.getUsername()) && userService.existsByUsername(request.getUsername().trim())) {
            bindingResult.rejectValue("username", "username.exists", "Пользователь с таким именем уже существует");
        }

        if (StringUtils.hasText(request.getEmail()) && userService.existsByEmail(request.getEmail().trim())) {
            bindingResult.rejectValue("email", "email.exists", "Пользователь с таким email уже существует");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(request);
        return "redirect:/login?registered";
    }
}
