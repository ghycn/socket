package com.huilan.socket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin
public class Index {
    @GetMapping("/index")
    public String index(){
        return "index";
    }
}
