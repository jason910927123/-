package org.example.wearegoodengineer.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class GoogleMapsService {

    private final GeoApiContext context;
    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleMapsService(@Value("${google.maps.apiKey}") String apiKey) {
        this.apiKey = apiKey;

        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }


    public List<Map<String, Object>> searchPlaces(String query, String saveDirectory) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            PlacesSearchResponse response = PlacesApi
                    .textSearchQuery(context, query)
                    .language("zh-TW")
                    .await();

            for (PlacesSearchResult place : response.results) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", place.name);
                map.put("address", place.formattedAddress);
                map.put("placeId", place.placeId);

                PlaceDetails details = getPlaceDetails(place.placeId);

                if (details != null) {
                    map.put("rating", details.rating);
                    map.put("openingHours", details.openingHours);

                    // 處理照片（使用 URL）
                    if (details.photos != null && details.photos.length > 0) {
                        String photoRef = details.photos[0].photoReference;
                        String localUrl = downloadPhoto(photoRef, saveDirectory);
                        map.put("photo", localUrl);
                    } else {
                        map.put("photo", null);
                    }
                }

                resultList.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }


    private PlaceDetails getPlaceDetails(String placeId) {
        try {
            return PlacesApi.placeDetails(context, placeId)
                    .language("zh-TW")
                    .await();
        } catch (Exception e) {
            return null;
        }
    }


    private String downloadPhoto(String photoReference, String saveDirectory) {
        try {
            String photoUrl =
                    "https://maps.googleapis.com/maps/api/place/photo"
                            + "?maxwidth=400"
                            + "&photo_reference=" + photoReference
                            + "&key=" + apiKey;

            byte[] bytes = restTemplate.getForObject(photoUrl, byte[].class);
            if (bytes == null) return null;

            String fileName = "photo_" + System.currentTimeMillis() + ".jpg";

            Path dirPath = Paths.get(saveDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path filePath = dirPath.resolve(fileName);
            Files.write(filePath, bytes);

            return filePath.toUri().toString();

        } catch (Exception e) {
            return null;
        }
    }

    public void shutdown() {
        context.shutdown();
    }
}
