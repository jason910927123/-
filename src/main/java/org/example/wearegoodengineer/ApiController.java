package org.example.wearegoodengineer;

//import org.example.wearegoodengineer.service.GoogleMapsService;
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
//    private GoogleMapsService googleMapsService;

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
    @PostMapping("/googlemaps")
    public ResponseEntity<?> googleMaps(@RequestBody Map<String, Object> data) {
        try {
            // 從請求資料中取得地點名稱
            String placeName = (String) data.get("place");
            if (placeName == null || placeName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Place name is required"));
            }

            // 調用 GoogleMapsService 的方法，根據地點名稱取得地點詳細資訊
//            Map<String, Object> responseList = googleMapsService.getPlaceDetailsByName(placeName);
//            return ResponseEntity.ok(Map.of("response", responseList));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
        return null;
    }

}
