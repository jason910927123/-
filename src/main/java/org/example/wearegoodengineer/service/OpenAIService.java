package org.example.wearegoodengineer.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PriceLevel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.Random;
import java.util.Map;
import java.util.Random;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${google.maps.apiKey}")
    private String googleMapsApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private GeoApiContext context;

    // 延迟初始化 GeoApiContext
    private GeoApiContext getContext() {
        if (this.context == null) {
            synchronized (this) {
                if (this.context == null) {
                    if (googleMapsApiKey == null || googleMapsApiKey.trim().isEmpty()) {
                        throw new IllegalStateException("Google Maps API key is not configured.");
                    }
                    this.context = new GeoApiContext.Builder()
                            .apiKey(googleMapsApiKey)
                            .build();
                }
            }
        }
        return this.context;
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

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 打印原始响应
            System.out.println("Raw OpenAI Response: " + response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("OpenAI API returned error: " + response.getStatusCode());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI API error: " + response.getStatusCode());
            }

            String travelPlanJson = processOpenAIResponse(response.getBody());

            // 添加价格信息
            return addPriceDetailsToTravelPlan(travelPlanJson);
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to call OpenAI API", e);
        }
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


        Random random = new Random();
        int startHour = 9;
        int endHour = 24;

        for (int i = 1; i <= totalDays; i++) {
            dailyPlanBuilder.append(String.format(
                    "{\n" +
                            "  \"day\": \"%d\",\n" +
                            "  \"date\": \"待生成日期\",\n" +
                            "  \"activities\": [\n", i));

            // 隨機生成具體的時間點
            for (int j = 1; j <= activityCountPerDay; j++) {
                // 隨機生成時間 (例如：09:00, 10:30, 14:00 等)
                int randomHour = random.nextInt(endHour - startHour + 1) + startHour;
                int randomMinute = random.nextInt(4) * 15;
                String timeOfDay = String.format("%02d:%02d", randomHour, randomMinute);

                dailyPlanBuilder.append(String.format(
                        "    {\"timeOfDay\": \"%s\", \"activity\": \"\", \"place\": \"\", \"description\": \"\", \"budgetAllocation\": \"\"}%s\n",
                        timeOfDay,
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
                            "  \"place\": \"\",\n" +
                            "  \"alternativeActivity\": \"\",\n" +
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
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI API response is empty");
        }

        try {
            System.out.println("Processing OpenAI Response: " + responseBody);

            JSONObject jsonResponse = new JSONObject(responseBody);
            if (!jsonResponse.has("choices") || jsonResponse.getJSONArray("choices").isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response: missing 'choices'");
            }

            JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
            if (!choice.has("message") || !choice.getJSONObject("message").has("content")) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response: missing 'message.content'");
            }

            return cleanJsonContent(choice.getJSONObject("message").getString("content"));

        } catch (Exception e) {
            System.err.println("Error parsing OpenAI response: " + responseBody);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse OpenAI API response", e);
        }
    }

    private String cleanJsonContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Generated content is empty");
        }

        return content.replaceAll("```json\\s*", "").replaceAll("\\s*```", "").trim();
    }

    private String addPriceDetailsToTravelPlan(String travelPlanJson) {
        JSONObject travelPlan = new JSONObject(travelPlanJson);

        JSONArray dailyPlan = travelPlan.optJSONArray("dailyPlan");
        if (dailyPlan == null) return travelPlan.toString();

        for (int i = 0; i < dailyPlan.length(); i++) {
            JSONObject dailyActivity = dailyPlan.getJSONObject(i);
            JSONArray activities = dailyActivity.optJSONArray("activities");
            if (activities == null) continue;

            for (int j = 0; j < activities.length(); j++) {
                JSONObject activity = activities.getJSONObject(j);
                String placeName = activity.optString("place", "");

                if (placeName.isEmpty()) {
                    System.err.println("Skipping activity with missing place name: " + activity);
                    continue;
                }

                String placeId = getPlaceId(placeName);
                if (placeId == null) {
                    System.err.println("Skipping activity due to missing placeId for place name: " + placeName);
                    continue;
                }

                PlaceDetails details = getPlaceDetailsFromGoogleMaps(placeId);
                if (details != null && details.priceLevel != null) {
                    activity.put("priceLevel", priceLevelToString(details.priceLevel));
                } else {
                    activity.put("priceLevel", "未知");
                }
            }
        }

        return travelPlan.toString();
    }

    private String getPlaceId(String placeName) {
        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(getContext(), placeName).await();
            if (response.results != null && response.results.length > 0) {
                return response.results[0].placeId;
            }
        } catch (Exception e) {
            System.err.println("Error fetching Place ID for place name: " + placeName + ", error: " + e.getMessage());
        }
        return null;
    }

    private PlaceDetails getPlaceDetailsFromGoogleMaps(String placeId) {
        try {
            PlaceDetails placeDetails = PlacesApi.placeDetails(getContext(), placeId).await();
            if (placeDetails != null) {
                // 获取常规的开放时间
                if (placeDetails.openingHours != null) {
                    System.out.println("Opening Hours:");

                    // 打印每天的开放时间
                    for (String day : placeDetails.openingHours.weekdayText) {
                        System.out.println(day);
                    }

                    // 如果有 "periods" 字段，可以打印
                    if (placeDetails.openingHours.periods != null) {
                        for (var period : placeDetails.openingHours.periods) {
                            System.out.println("Day: " + period.open.day + ", Open: " + period.open.time + ", Close: " + period.close.time);
                        }
                    }
                }
            }
            return placeDetails;
        } catch (Exception e) {
            System.err.println("Error fetching Place details for placeId: " + placeId + ", error: " + e.getMessage());
        }
        return null;
    }




    private String priceLevelToString(PriceLevel priceLevel) {
        switch (priceLevel) {
            case FREE: return "免費";
            case INEXPENSIVE: return "低價（約 100-300 元）";
            case MODERATE: return "中價（約 300-600 元）";
            case EXPENSIVE: return "高價（約 600-1000 元）";
            case VERY_EXPENSIVE: return "非常高價（約 1000 元以上）";
            default: return "未知";
        }
    }
}

