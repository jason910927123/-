package org.example.wearegoodengineer;

import org.example.wearegoodengineer.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private OpenAIService openAIService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @PostMapping("/moreopenai")
    public ResponseEntity<?> moreOpenAI(@RequestBody Map<String, Object> data) {
        try {
            // 調用服務層來處理具體邏輯
            List<Map<String, Object>> responseList = openAIService.generateTravelPlan(data);
            return ResponseEntity.ok(Map.of("response", responseList));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
