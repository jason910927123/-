package org.example.wearegoodengineer.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PhotoRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.ImageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoogleMapsService {

    private final GeoApiContext context;

    // 建構子 - 初始化 GeoApiContext 並設置 API Key
    public GoogleMapsService(@Value("${google.maps.apiKey}") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    // Place Text Search 方法
    public List<Map<String, Object>> searchPlaces(String query, String saveDirectory) {
        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(context, query)
                    .language("zh-TW") // 設定語言為繁體中文
                    .await();
            PlacesSearchResult[] results = response.results;
            List<Map<String, Object>> placeInfoList = new ArrayList<>();

            for (PlacesSearchResult result : results) {
                Map<String, Object> placeInfo = new HashMap<>();
                placeInfo.put("place", result.name);
                placeInfo.put("formattedAddress", result.formattedAddress);
                placeInfo.put("placeId", result.placeId);

                // 取得地點的詳細資訊並顯示評分
                PlaceDetails details = getPlaceDetails(result.placeId);
                if (details != null) {
                    placeInfo.put("rating", details.rating);
                    placeInfo.put("openingHours", details.openingHours);

                    // 取得並儲存圖片
                    if (details.photos != null && details.photos.length > 0) {
                        String photoUrl = getPlacePhoto(details.photos[0].photoReference, saveDirectory);
                        placeInfo.put("photos", photoUrl);
                    } else {
                        placeInfo.put("photos", null);
                    }
                } else {
                    placeInfo.put("rating", null);
                    placeInfo.put("openingHours", null);
                    placeInfo.put("photos", null);
                }

                placeInfoList.add(placeInfo);
            }

            return placeInfoList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 取得地點的詳細資訊
    private PlaceDetails getPlaceDetails(String placeId) {
        try {
            return PlacesApi.placeDetails(context, placeId)
                    .language("zh-TW") // 設定語言為繁體中文
                    .await();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 取得地點的第一張圖片並儲存
    private String getPlacePhoto(String photoReference, String saveDirectory) {
        try {
            PhotoRequest photoRequest = new PhotoRequest(context).photoReference(photoReference).maxWidth(400);
            ImageResult imageResult = photoRequest.await();
            if (imageResult != null && imageResult.imageData != null) {
                Path filePath = Paths.get(saveDirectory, "photo_" + photoReference + ".jpg");
                Files.write(filePath, imageResult.imageData);
                return filePath.toUri().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 關閉資源
    public void shutdown() {
        context.shutdown();
    }
}
