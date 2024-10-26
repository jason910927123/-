package org.example.wearegoodengineer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @PostMapping("/moreopenai")
    public ResponseEntity<?> moreOpenAI(@RequestBody Map<String, Object> data) {
        try {
            // 確認data不為空
            if (data == null || data.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No data provided");
            }

            // 解析並檢查所需字段
            String budget = (String) data.get("budget");
            String purpose = (String) data.get("purpose");
            String season = (String) data.get("season");
            String day = (String) data.get("day");
            String place = (String) data.get("place");

            if (budget == null || purpose == null || season == null || day == null || place == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing one or more required fields");
            }

            // 構建提示內容
            String prompt = String.format(
                    "請幫我規劃一個預算為%s元新台幣的旅行行程，目的為%s，出發日期是%s，計畫旅行%s，想去的地方是%s。請提供詳細的行程安排並給我餐廳的電話跟經緯度。",
                    budget, purpose, season, day, place
            );

            // 配置請求頭和負載
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + openaiApiKey);

            JSONObject payload = new JSONObject();
            payload.put("model", "gpt-4o");
            payload.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", prompt)));
            payload.put("max_tokens", 550);

            HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);

            // 發送請求到OpenAI API
            String url = "https://api.openai.com/v1/chat/completions";
            ResponseEntity<String> openaiResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 解析並回傳結果
            JSONObject responseJson = new JSONObject(openaiResponse.getBody());
            if (responseJson.has("choices") && responseJson.getJSONArray("choices").length() > 0) {
                String responseText = responseJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim();
                return ResponseEntity.ok(Map.of("response", responseText));
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response from OpenAI");
            }

        } catch (ResponseStatusException e) {
            System.out.println("Error: " + e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            System.out.println("Other error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
