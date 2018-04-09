package com.khudim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping(value = {"/"})
    public String redirect() {
        return "redirect:/index";
    }

    @GetMapping(value = {"/index", "/index/content/**"})
    public String index() {
        return "index";
    }
}
