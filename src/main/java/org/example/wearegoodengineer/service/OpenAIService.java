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
        // 檢查傳入的資料是否為空
        if (data == null || data.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No data provided");
        }

        // 提取必須的資料字段
        String budget = String.valueOf(data.get("budget"));
        String purpose = String.valueOf(data.get("purpose"));
        String startDate = String.valueOf(data.get("startDate"));
        String endDate = String.valueOf(data.get("endDate"));
        String day = String.valueOf(data.get("day"));
        String place = String.valueOf(data.get("place"));

        // 檢查是否有缺少的必要字段
        if (budget == null || purpose == null || startDate == null || endDate == null || day == null || place == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing one or more required fields");
        }

        // 生成 prompt
        String prompt = generatePrompt(budget, purpose, startDate, endDate, day, place);

        // 配置請求頭和負載
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + openaiApiKey);

        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-4o-mini");
        payload.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", "幫我補全我以下的JSON(Only json)" + prompt)));
        payload.put("max_tokens", 2000);

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);

        // 發送請求到 OpenAI API
        String url = "https://api.openai.com/v1/chat/completions";
        ResponseEntity<String> openaiResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // 檢查 API 回應的有效性
        String responseBody = openaiResponse.getBody();
        if (responseBody == null || responseBody.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Empty response from OpenAI API");
        }

        try {
            // 解析 JSON 字串
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (!jsonResponse.has("choices") || jsonResponse.getJSONArray("choices").length() == 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No valid choices in OpenAI response");
            }

            // 獲取 "choices" 陣列的第一個元素
            JSONObject firstChoice = jsonResponse.getJSONArray("choices").getJSONObject(0);
            String content = firstChoice.getJSONObject("message").getString("content");

            // 清理 content 中的無用部分
            String cleanContent = cleanJsonContent(content);

            // 解析清理後的 JSON
            JSONObject jsonObject = new JSONObject(cleanContent);
            return jsonObject.toString();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing response JSON", e);
        }
    }

    // 生成 prompt
    private String generatePrompt(String budget, String purpose, String startDate, String endDate, String day, String place) {
        return String.format(
                "{\n" +
                        "  \"request\": {\n" +
                        "    \"prompt\": {\n" +
                        "      \"budget\": \"%s\",\n" +
                        "      \"purpose\": \"%s\",\n" +
                        "      \"startDate\": \"%s\",\n" +
                        "      \"endDate\": \"%s\",\n" +
                        "      \"durationDays\": \"%s\",\n" +
                        "      \"destination\": \"%s\",\n" +
                        "      \"details\": {\n" +
                        "        \"overview\": \"作為一位專業的旅行規劃師，請根據以下條件設計一個詳細的旅行行程：\",\n" +
                        "        \"budgetLimit\": \"%s\",\n" +
                        "        \"travelPurpose\": \"%s\",\n" +
                        "        \"startDate\": \"%s\",\n" +
                        "        \"plannedDuration\": \"%s 天\",\n" +
                        "        \"destination\": \"%s\",\n" +
                        "        \"requirements\": [\n" +
                        "          \"每日的具體行程安排，包括上午、下午和晚間的活動建議。\",\n" +
                        "          \"推薦的景點（至少三個），並附上簡短的描述和適合的時間段。\",\n" +
                        "          \"餐廳建議，經緯度位置。\",\n" +
                        "          \"每日預算分配和建議開銷，以確保符合整體預算限制。\"\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"dailyPlan\": [\n" +
                        "    {\n" +
                        "      \"day\": \"1\",\n" +
                        "      \"date\": \"%s\",\n" +
                        "      \"activities\": [\n" +
                        "        {\n" +
                        "          \"timeOfDay\": \"Morning\",\n" +
                        "          \"activity\": \"%s\",\n" +
                        "          \"description\": \"\",\n" +
                        "          \"suggestedTime\": \"\",\n" +
                        "          \"budgetAllocation\": \"\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"timeOfDay\": \"Noon\",\n" +
                        "          \"activity\": \"%s\",\n" +
                        "          \"description\": \"\",\n" +
                        "          \"suggestedTime\": \"\",\n" +
                        "          \"budgetAllocation\": \"\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"timeOfDay\": \"Night\",\n" +
                        "          \"activity\": \"%s\",\n" +
                        "          \"description\": \"\",\n" +
                        "          \"suggestedTime\": \"\",\n" +
                        "          \"budgetAllocation\": \"\"\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"recommendedRestaurants\": [\n" +
                        "        {\n" +
                        "          \"name\": \"\",\n" +
                        "          \"latitude\": \"\",\n" +
                        "          \"longitude\": \"\"\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"dailyBudget\": \"\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"budgetSummary\": {\n" +
                        "    \"totalBudget\": \"%s\",\n" +
                        "    \"dailyAverage\": \"\",\n" +
                        "    \"remainingBudget\": \"\",\n" +
                        "    \"expenses\": [\n" +
                        "      {\n" +
                        "        \"category\": \"\",\n" +
                        "        \"estimatedCost\": \"\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"sparePlan\": [\n" +
                        "    {\n" +
                        "      \"name\": \"\",\n" +
                        "      \"description\": \"\",\n" +
                        "      \"bestTime\": \"\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "} ",
                budget, purpose, startDate, endDate, day, place,
                budget, purpose, startDate, day, place,
                startDate, "早上活動", "中午活動", "晚上活動",
                budget
        );
    }

    // 清理 content 字符串中的無用部分
    private String cleanJsonContent(String content) {
        // 1. 移除 Markdown 語法
        content = content.replace("```json\n", "").replace("\n```", "");

        // 2. 去除多餘的空格
        content = content.replaceAll("\\s*\\{\\s*", "{")
                .replaceAll("\\s*\\}\\s*", "}")
                .replaceAll("\\s*\\[\\s*", "[")
                .replaceAll("\\s*\\]\\s*", "]");

        // 3. 驗證 JSON 完整性
        try {
            // 嘗試將清理後的字串轉換成 JSON 物件
            new JSONObject(content);
        } catch (Exception e) {
            // 如果解析失敗，回傳錯誤信息
            throw new IllegalArgumentException("JSON 格式不完整或有錯誤: " + e.getMessage());
        }

        return content;
    }
}

