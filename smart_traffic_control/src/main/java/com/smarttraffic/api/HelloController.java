package com.smarttraffic.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/api/hello")
    public String hello() {
        return "Servidor Spring Boot funcionando! 🚦";
    }
}
    