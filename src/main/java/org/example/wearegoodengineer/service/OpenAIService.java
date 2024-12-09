package org.example.wearegoodengineer.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PriceLevel;
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

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${google.maps.apiKey}")
    private String googleMapsApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private GeoApiContext context;

    @PostConstruct
    public void initGeoApiContext() {
        if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
            throw new IllegalStateException("Google Maps API key is not configured");
        }
        this.context = new GeoApiContext.Builder()
                .apiKey(googleMapsApiKey)
                .build();
    }

    public String generateTravelPlan(Map<String, Object> data) {
        validateInput(data);

        // 提取数据
        String budget = String.valueOf(data.get("budget"));
        String purpose = String.valueOf(data.get("purpose"));
        String startDate = String.valueOf(data.get("startDate"));
        String endDate = String.valueOf(data.get("endDate"));
        String day = String.valueOf(data.get("day"));
        String place = String.valueOf(data.get("place"));
        String commuting = String.valueOf(data.get("commuting"));

        int sparePlanCount = data.containsKey("sparePlanCount") ?
                Integer.parseInt(String.valueOf(data.get("sparePlanCount"))) : 3;

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

        String travelPlanJson = processOpenAIResponse(openaiResponse.getBody());

        return addPriceDetailsToTravelPlan(travelPlanJson);
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

    // 從 Google Maps API 取得地點價格等級並加入行程中
    private String addPriceDetailsToTravelPlan(String travelPlanJson) {
        JSONObject travelPlan = new JSONObject(travelPlanJson);

        // 遍歷每日行程，為每個活動加入價格等級
        JSONArray dailyPlan = travelPlan.getJSONArray("dailyPlan");
        for (int i = 0; i < dailyPlan.length(); i++) {
            JSONObject dailyActivity = dailyPlan.getJSONObject(i);
            JSONArray activities = dailyActivity.getJSONArray("activities");
            for (int j = 0; j < activities.length(); j++) {
                JSONObject activity = activities.getJSONObject(j);
                String placeId = activity.getString("place"); // 假設每個活動的 place 欄位包含 Google Maps 的 Place ID

                // 確保 placeId 存在且有效
                if (placeId != null && !placeId.isEmpty()) {
                    double priceLevel = getPlacePriceFromGoogleMaps(placeId);
                    activity.put("priceLevel", priceLevel); // 在每個活動中加入價格等級
                }
            }
        }

        return travelPlan.toString();
    }

    // 取得 Google Maps 中地點的價格等級
    private double getPlacePriceFromGoogleMaps(String placeId) {
        try {
            PlaceDetails placeDetails = PlacesApi.placeDetails(context, placeId).await();
            if (placeDetails != null && placeDetails.priceLevel != null) {
                return priceLevelToDouble(placeDetails.priceLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1.0; // 返回 -1 代表未找到有效的價格資訊
    }

    // 將價格等級轉換為數字
    private double priceLevelToDouble(PriceLevel priceLevel) {
        switch (priceLevel) {
            case FREE:
                return 0.0;
            case INEXPENSIVE:
                return 1.0;
            case MODERATE:
                return 2.0;
            case EXPENSIVE:
                return 3.0;
            case VERY_EXPENSIVE:
                return 4.0;
            default:
                return -1.0; // 返回 -1 代表未找到有效的價格等級
        }
    }

    // 將價格等級轉換為價格範圍
    private String priceLevelToEstimatedPriceRange(PriceLevel priceLevel) {
        switch (priceLevel) {
            case FREE:
                return "免費";
            case INEXPENSIVE:
                return "低價（約 100-300 元）";
            case MODERATE:
                return "中價（約 300-600 元）";
            case EXPENSIVE:
                return "高價（約 600-1000 元）";
            case VERY_EXPENSIVE:
                return "非常高價（約 1000 元以上）";
            default:
                return "未知";
        }
    }



    // 關閉資源
    public void shutdown() {
        context.shutdown();
    }
}
