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
        // 檢查傳入資料
        validateInput(data);

        // 提取資料字段
        String budget = String.valueOf(data.get("budget"));
        String purpose = String.valueOf(data.get("purpose"));
        String startDate = String.valueOf(data.get("startDate"));
        String endDate = String.valueOf(data.get("endDate"));
        String day = String.valueOf(data.get("day"));
        String place = String.valueOf(data.get("place"));
        String commuting = String.valueOf(data.get("commuting"));


        int sparePlanCount = data.containsKey("sparePlanCount") ?
                Integer.parseInt(String.valueOf(data.get("sparePlanCount"))) : 3;

        // 每日活動數量（預設為 5）
        int activityCountPerDay = data.containsKey("activityCountPerDay") ?
                Integer.parseInt(String.valueOf(data.get("activityCountPerDay"))) : 5;


        String prompt = generatePrompt(budget, purpose, startDate, endDate, day, place, commuting, sparePlanCount, activityCountPerDay);


        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + openaiApiKey);

        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-4o-mini");
        payload.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", prompt)));
        payload.put("max_tokens", 3000);

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);


        String url = "https://api.openai.com/v1/chat/completions";
        ResponseEntity<String> openaiResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);


        return processOpenAIResponse(openaiResponse.getBody());
    }

    private void validateInput(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No data provided");
        }

        if (!data.containsKey("budget") || !data.containsKey("purpose") ||
                !data.containsKey("startDate") || !data.containsKey("endDate") ||
                !data.containsKey("day") || !data.containsKey("place") || !data.containsKey("commuting")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing one or more required fields");
        }
    }

    private String generatePrompt(String budget, String purpose, String startDate, String endDate, String day, String place, String commuting, int sparePlanCount, int activityCountPerDay) {
        StringBuilder dailyPlanBuilder = new StringBuilder();
        int totalDays = Integer.parseInt(day);


        for (int i = 1; i <= totalDays; i++) {
            dailyPlanBuilder.append(String.format(
                    "{\n" +
                            "  \"day\": \"%d\",\n" +
                            "  \"date\": \"待生成日期\",\n" +
                            "  \"activities\": [\n", i));


            for (int j = 1; j <= activityCountPerDay; j++) {
                dailyPlanBuilder.append(String.format(
                        "    {\"timeOfDay\": \"Activity %d\", \"activity\": \"\", \"place\": \"\", \"description\": \"\", \"budgetAllocation\": \"\"}%s\n",
                        j,
                        j < activityCountPerDay ? "," : ""
                ));
            }

            dailyPlanBuilder.append(
                    "  ],\n" +
                            "  \"recommendedRestaurants\": [],\n" +
                            "  \"dailyBudget\": \"\"\n" +
                            "},");
        }


        StringBuilder sparePlanBuilder = new StringBuilder();
        for (int i = 1; i <= sparePlanCount; i++) {
            sparePlanBuilder.append(String.format(
                    "{\n" +
                            "  \"reason\": \"備用方案原因 %d\",\n" +
                            "  \"alternativeActivity\": \"\",\n" +
                            "  \"place\": \"\",\n" +
                            "  \"budgetAllocation\": \"\"\n" +
                            "}", i));

            if (i < sparePlanCount) {
                sparePlanBuilder.append(",");
            }
        }


        return String.format(
                "請幫我規劃一個完整的旅遊行程，包括每日行程與備用計劃，條件如下：\n" +
                        "1. 預算：%s 元\n" +
                        "2. 目的：%s\n" +
                        "3. 旅遊日期：%s 至 %s\n" +
                        "4. 總天數：%s 天\n" +
                        "5. 目的地：%s\n" +
                        "6. 交通方式：%s\n" +
                        "請以 JSON 格式返回，範例格式如下：\n" +
                        "{\n" +
                        "  \"dailyPlan\": [%s],\n" +
                        "  \"sparePlan\": [%s]\n" +
                        "}",
                budget, purpose, startDate, endDate, day, place, commuting, dailyPlanBuilder.toString(), sparePlanBuilder.toString());
    }

    private String processOpenAIResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI API response is empty");
        }

        JSONObject jsonResponse = new JSONObject(responseBody);
        if (!jsonResponse.has("choices") || jsonResponse.getJSONArray("choices").isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response structure: missing 'choices'");
        }

        JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
        if (!choice.has("message") || !choice.getJSONObject("message").has("content")) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response structure: missing 'message.content'");
        }

        String content = choice.getJSONObject("message").getString("content");


        return cleanJsonContent(content);
    }

    private String cleanJsonContent(String content) {
        if (content == null || content.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Generated content is empty");
        }


        String cleanContent = content.replaceAll("```json\\s*", "").replaceAll("\\s*```", "").trim();


        try {
            new JSONObject(cleanContent);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cleaned content is not valid JSON: " + cleanContent, e);
        }

        return cleanContent;
    }
}
