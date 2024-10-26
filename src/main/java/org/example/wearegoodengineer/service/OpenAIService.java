package org.example.wearegoodengineer.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateTravelPlan(Map<String, Object> data) {
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
                "作為一位專業的旅行規劃師，請根據以下條件設計一個詳細的旅行行程：\n" +
                        "- 預算限制：%s 新台幣\n" +
                        "- 旅行目的：%s（如休閒度假、冒險探索、美食之旅等）\n" +
                        "- 出發日期和季節：%s\n" +
                        "- 計劃行程天數：%s 天\n" +
                        "- 旅行目的地：%s\n\n" +
                        "請提供以下詳細內容：\n" +
                        "1. 每日的具體行程安排，包括上午、下午和晚間的活動建議。\n" +
                        "2. 推薦的景點（至少三個），並附上簡短的描述和適合的時間段。\n" +
                        "3. 餐廳建議，包括名稱、菜系、平均價格、聯繫電話以及經緯度位置。\n" +
                        "4. 每日預算分配和建議開銷，以確保符合整體預算限制。\n\n" +
                        "請用清晰條理的方式呈現行程，並確保資訊精確且符合專業水準。",
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
            return responseJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response from OpenAI");
        }
    }
}
