package com.maistruk.sweater.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.maistruk.sweater.domain.Role;
import com.maistruk.sweater.domain.User;
import com.maistruk.sweater.repos.UserRepo;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final MyMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${host.root}")
    private String host;

    public UserService(UserRepo userRepo, MyMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUserName(username);
        if(user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    public boolean addUser(User user) {
        User userFromDb = userRepo.findByUserName(user.getUserName());
        if (userFromDb != null) {
            return false;
        }

        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepo.save(user);

        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Hello, %s! \nWelcome to Sweater. " + "Please visit next link: http://%s/activate/%s", user.getUserName(),
                    host, user.getActivationCode());
            mailSender.send(user.getEmail(), "Activation code", message);
        }
        return true;
    }

    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);
        if (user == null) {
            return false;
        } else {
            user.setActive(true);
            user.setActivationCode(null);
            userRepo.save(user);

            return true;
        }
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public void saveUser(User user, String userName, Map<String, String> form) {
        user.setUserName(userName);
        Set<String> roles = Arrays.stream(Role.values()).map(Role::name).collect(Collectors.toSet());

        user.getRoles().clear();
        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }
    }

    public void updateProfile(User user, String password, String email) {
        String userEmail = user.getEmail();

        boolean isEmailChanged = (email != null && userEmail != null && !email.equals(userEmail) 
                && !email.isEmpty());

        if (isEmailChanged) {
            user.setEmail(email);
            user.setActivationCode(UUID.randomUUID().toString());
        }

        if (!StringUtils.isEmpty(password)) {
            user.setPassword(password);
        }
        
        if(isEmailChanged) {
            sendMessage(user);
        }
        userRepo.save(user);

    }

    public void sendMessage(User user) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Hello, %s! \nWelcome to Sweater. " + "Please visit next link: %s/activate/%s", user.getUserName(),
                    host, user.getActivationCode());
            mailSender.send(user.getEmail(), "Activation code", message);
        }
    }

}
