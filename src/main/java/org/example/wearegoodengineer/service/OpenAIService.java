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
        // 確認 data 不為空
        if (data == null || data.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No data provided");
        }

        // 提取資料字段
        String budget = String.valueOf(data.get("budget"));
        String purpose = String.valueOf(data.get("purpose"));
        String startDate = String.valueOf(data.get("startDate"));
        String endDate = String.valueOf(data.get("endDate"));
        String day = String.valueOf(data.get("day"));
        String place = String.valueOf(data.get("place"));

// 檢查字段是否齊全
        if (budget == null || purpose == null || startDate == null||endDate==null || day == null || place == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing one or more required fields");
        }

// 使用 JSON 格式構建提示內容
        String prompt = String.format(
                "{\n" +
                        "  \"request\": {\n" +
                        "    \"prompt\": {\n" +
                        "      \"budget\": \"%s\",\n" +
                        "      \"purpose\": \"%s\",\n" +
                        "      \"startDate\": \"%s\",\n" +
                        "      \"endDate\": \"%s\",\n" +
                        "      \"durationDays\": %s,\n" +
                        "      \"destination\": \"%s\",\n" +
                        "      \"details\": {\n" +
                        "        \"overview\": \"作為一位專業的旅行規劃師，請根據以下條件設計一個詳細的旅行行程：\",\n" +
                        "        \"budgetLimit\": \"%s 新台幣\",\n" +
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
                        "  \"responseFormat\": {\n" +
                        "    \"itinerary\": {\n" +
                        "      \"overview\": {\n" +
                        "        \"budget\": \"\",\n" +
                        "        \"purpose\": \"\",\n" +
                        "        \"startDate\": \"\",\n" +
                        "        \"endDate\": \"\",\n" +
                        "        \"durationDays\": \"\",\n" +
                        "        \"destination\": \"\",\n" +
                        "        \"summary\": \"\"\n" +
                        "      },\n" +
                        "      \"dailyPlan\": [\n" +
                        "        {\n" +
                        "          \"day\": \"\",\n" +
                        "          \"date\": \"\",\n" +
                        "          \"activities\": [\n" +
                        "            {\n" +
                        "              \"timeOfDay\": \"\",\n" +
                        "              \"activity\": \"\",\n" +
                        "              \"description\": \"\",\n" +
                        "              \"suggestedTime\": \"\",\n" +
                        "              \"budgetAllocation\": \"\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"recommendedRestaurants\": [\n" +
                        "            {\n" +
                        "              \"name\": \"\",\n" +
                        "              \"latitude\": \"\",\n" +
                        "              \"longitude\": \"\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"dailyBudget\": \"\"\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"budgetSummary\": {\n" +
                        "        \"totalBudget\": \"\",\n" +
                        "        \"dailyAverage\": \"\",\n" +
                        "        \"remainingBudget\": \"\",\n" +
                        "        \"expenses\": [\n" +
                        "          {\n" +
                        "            \"category\": \"\",\n" +
                        "            \"estimatedCost\": \"\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      },\n" +
                        "      \"recommendedAttractions\": [\n" +
                        "        {\n" +
                        "          \"name\": \"\",\n" +
                        "          \"description\": \"\",\n" +
                        "          \"bestTime\": \"\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                budget, purpose, startDate, endDate, day, place,
                budget, purpose, startDate, day, place
        );

        // 配置請求頭和負載
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + openaiApiKey);

        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-4o");
        System.out.println("送出的資料:\n"+ prompt);
        payload.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", "幫我補全我以下的JSON(Only json)"+prompt)));
        payload.put("max_tokens", 550);

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);

        // 發送請求到 OpenAI API
        String url = "https://api.openai.com/v1/chat/completions";
        ResponseEntity<String> openaiResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(openaiResponse.getBody());
        //檢查回送內容
        String responseBody = openaiResponse.getBody();  // 獲取 API 回應的 JSON 字串
        try {
            // 解析 JSON 字串
            JSONObject jsonResponse = new JSONObject(responseBody);

            // 獲取 "choices" 陣列的第一個元素
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            JSONObject firstChoice = choicesArray.getJSONObject(0);

            // 提取 "message" 裡的 "content"
            String content = firstChoice.getJSONObject("message").getString("content");

            // 輸出 content
            System.out.println(content);
            return content;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing response JSON", e);
        }
        // 解析並回傳結果
//        JSONObject responseJson = new JSONObject(openaiResponse.getBody());
//        if (responseJson.has("choices") && responseJson.getJSONArray("choices").length() > 0) {
//            String content = responseJson.getJSONArray("choices")
//                    .getJSONObject(0)
//                    .getJSONObject("message")
//                    .getString("content")
//                    .trim();
//
//            // 假設回應內容為 JSON 格式的行程資料，解析並將其存入陣列
//            JSONArray travelArray = new JSONArray(content);
//            List<Map<String, Object>> travelDataList = new ArrayList<>();
//
//            for (int i = 0; i < travelArray.length(); i++) {
//                JSONObject travelItem = travelArray.getJSONObject(i);
//
//                // 提取所需字段
//                String date = travelItem.optString("date", "");  // 日期
//                String time = travelItem.optString("time", "");  // 時間
//                String placeName = travelItem.optString("place", "");  // 地點
//                double latitude = travelItem.optDouble("latitude", 0);  // 緯度
//                double longitude = travelItem.optDouble("longitude", 0);  // 經度
//
//                // 封裝成 Map
//                Map<String, Object> travelData = new HashMap<>();
//                travelData.put("date", date);
//                travelData.put("time", time);
//                travelData.put("place", placeName);
//                travelData.put("latitude", latitude);
//                travelData.put("longitude", longitude);
//
//                travelDataList.add(travelData);
//            }
//            return travelDataList;
//        } else {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response from OpenAI");
//        }
    }
}
