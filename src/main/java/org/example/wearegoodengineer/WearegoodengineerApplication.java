package org.example.wearegoodengineer;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

//http://localhost:8080/swagger-ui.html 訪問ui api
//http://localhost:8080/v3/api-docs 訪問api文檔
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "My API", version = "1.0", description = "API for OpenAI travel planning"))
public class WearegoodengineerApplication {

    public static void main(String[] args) {

        SpringApplication.run(WearegoodengineerApplication.class, args);
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("/index.html");
    }
}
