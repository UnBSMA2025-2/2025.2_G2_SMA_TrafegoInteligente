package com.smarttraffic.api; // Mesmo pacote dos outros arquivos

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Libera o acesso HTTP do front-end (porta 5173)
        // para o back-end (porta 8080)
        registry.addMapping("/**") // Para todos os endpoints
                .allowedOrigins("http://localhost:5173") // A origem do seu front-end
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}