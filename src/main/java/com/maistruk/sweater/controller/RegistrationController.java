package com.maistruk.sweater.controller;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.maistruk.sweater.domain.User;
import com.maistruk.sweater.domain.dto.CaptchaResponseDto;
import com.maistruk.sweater.service.UserService;

@Controller
public class RegistrationController {
    
    private final static String CAPTCH_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";
    
    @Value("${recaptcha.secret}")
    private String recaptchaSecret;
    
    private UserService userService;
    private RestTemplate restTemplate;

    public RegistrationController(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/registration")
    public ModelAndView registration() {
        return new ModelAndView("registration");
    }
    
    @PostMapping("/registration")
    public ModelAndView addUser(@RequestParam("g-recaptcha-response") String captchaResponse, @RequestParam("password2") String passwordConfirmation, @Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        String url = String.format(CAPTCH_URL, recaptchaSecret, captchaResponse);
        CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);
        if(!response.isSuccess()) {
            modelAndView.addObject("captchaError", "Fill captcha");
        }
        boolean isPassword2Empty = StringUtils.isEmpty(passwordConfirmation);
        if(isPassword2Empty) {
            modelAndView.addObject("password2Error", "Password confirmation cannot be empty");
        }
        
        if(user.getPassword() != null && !user.getPassword().equals(passwordConfirmation)) {
            modelAndView.addObject("passwordError", "passwords not equils");
        }
        if(isPassword2Empty || bindingResult.hasErrors() || !response.isSuccess()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            modelAndView.addAllObjects(errors);
            modelAndView.setViewName("registration");
            return modelAndView;
        }
        if(!userService.addUser(user)) {
            modelAndView.addObject("userNameError", "User exist");
            modelAndView.setViewName("registration");
            return modelAndView;
        }
        modelAndView.setViewName("redirect:/login");
        return modelAndView;
    }
    
    @GetMapping("/activate/{code}")
    public ModelAndView activate(@PathVariable String code) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        boolean isActivated = userService.activateUser(code);
        if(isActivated) {
            modelAndView.addObject("message", "User successfully activated");
            modelAndView.addObject("messageType", "success");
        } else {
            modelAndView.addObject("message", "Activation code not found");
            modelAndView.addObject("messageType", "danger");

        }
        return modelAndView;
    }

}
