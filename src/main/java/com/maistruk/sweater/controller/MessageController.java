package com.maistruk.sweater.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.maistruk.sweater.domain.Message;
import com.maistruk.sweater.domain.User;
import com.maistruk.sweater.repos.MessageRepo;

@Controller
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageRepo messageRepo;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/main")
    public ModelAndView greeting(@RequestParam(name = "filter", required = false, defaultValue = "") String filter) {
        ModelAndView modelAndView = new ModelAndView("messages");
        modelAndView.addObject("filter", filter);
        List<Message> messages;
        if (filter == null || filter.isEmpty()) {
            messages = (List<Message>) messageRepo.findAll();
        } else {
            messages = messageRepo.findByTag(filter);
        }
        modelAndView.addObject("messages", messages);
        return modelAndView;
    }

    @PostMapping("/create")
    public ModelAndView create(@AuthenticationPrincipal User user, @Valid Message message, BindingResult bindingResult,
            @RequestParam("file") MultipartFile file) throws IllegalStateException, IOException {
        ModelAndView modelAndView = new ModelAndView("messages");
        message.setAuthor(user);

        if (bindingResult.hasErrors()) {
            Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);
            System.out.println(errorsMap);
            modelAndView.addAllObjects(errorsMap);
            modelAndView.addObject("message", message);
        } else {
            if (file != null && !file.getOriginalFilename().isEmpty()) {
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }
                String uuidFile = UUID.randomUUID().toString();
                String resultFileName = uuidFile + "." + file.getOriginalFilename();

                file.transferTo(new File(uploadPath + "/" + resultFileName));
                message.setFileName(resultFileName);
            }
            modelAndView.addObject("message", null);
            messageRepo.save(message);
        }

        List<Message> messages = (List<Message>) messageRepo.findAll();
        modelAndView.addObject("messages", messages);
        return modelAndView;
    }

    // @PostMapping("/filter")
    // public ModelAndView filter(@RequestParam String filter) {
    // List<Message> messages;
    // if(filter == null || filter.isEmpty()) {
    // messages = (List<Message>) messageRepo.findAll();
    // } else {
    // messages = messageRepo.findByTag(filter);
    // }
    // ModelAndView modelAndView = new ModelAndView("messages");
    // modelAndView.addObject("messages", messages);
    // return modelAndView;
    // }
    


}
