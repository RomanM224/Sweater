package com.maistruk.sweater.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.maistruk.sweater.domain.Role;
import com.maistruk.sweater.domain.User;
import com.maistruk.sweater.repos.UserRepo;
import com.maistruk.sweater.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping()
    public ModelAndView userList() {
        ModelAndView modelAndView = new ModelAndView();
        List<User> users = userService.findAll();
        modelAndView.addObject("users", users);
        modelAndView.setViewName("userList");
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("{user}")
    public ModelAndView userEditForm(@PathVariable User user) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        List<String> roles = new ArrayList<>();
        for (Role role : Role.values()) {
            roles.add(role.toString());
        }
        modelAndView.addObject("roles", roles);
        List<String> userRoles = new ArrayList<>();
        for (Role role : user.getRoles()) {
            userRoles.add(role.toString());
        }
        modelAndView.addObject("userRoles", userRoles);
        modelAndView.setViewName("userEdit");
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ModelAndView userSave(@RequestParam String userName, @RequestParam Map<String, String> form,
            @RequestParam("userId") User user) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/user");
        
        userService.saveUser(user, userName, form);
        return modelAndView;
    }
    
    @GetMapping("/profile")
    public ModelAndView getProfile(@AuthenticationPrincipal User user) {
        ModelAndView modelAndView = new ModelAndView("profile");
        modelAndView.addObject("userName", user.getUsername());
        modelAndView.addObject("email", user.getEmail());

        return modelAndView;
    }
    
    @PostMapping("/profile")
    public ModelAndView updateProfile(@AuthenticationPrincipal User user, 
            @RequestParam String password, @RequestParam String email) {
        ModelAndView modelAndView = new ModelAndView("redirect:/user/profile");
        userService.updateProfile(user, password, email);
        
        return modelAndView;
    }

}
