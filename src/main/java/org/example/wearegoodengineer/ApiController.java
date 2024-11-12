package org.example.wearegoodengineer;

import com.google.maps.model.PlacesSearchResult;
//import org.example.wearegoodengineer.service.GoogleMapsService;
import org.example.wearegoodengineer.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final OpenAIService openAIService;
//    private final GoogleMapsService googleMapsService;

    // 使用構造函數注入 OpenAIService 和 GoogleMapsService
    @Autowired
    public ApiController(OpenAIService openAIService, GoogleMapsService googleMapsService) {
        this.openAIService = openAIService;
//        this.googleMapsService = googleMapsService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @PostMapping("/moreopenai")
    public ResponseEntity<?> moreOpenAI(@RequestBody Map<String, Object> data) {
        try {
            // 調用服務層來處理具體邏輯
            String responseList = openAIService.generateTravelPlan(data);
            return ResponseEntity.ok(Map.of("response", responseList));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/googlemaps")
    public ResponseEntity<?> googleMaps(@RequestBody Map<String, Object> data) {
        try {
            String placeName = (String) data.get("place");
            if (placeName == null || placeName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Place name is required"));
            }

            String saveDirectory = System.getProperty("java.io.tmpdir");

            // 調用 GoogleMapsService 的方法，根據地點名稱取得地點詳細資訊
            List<Map<String, Object>> searchResults = googleMapsService.searchPlaces(placeName, saveDirectory);

            return ResponseEntity.ok(Map.of("results", searchResults));
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
