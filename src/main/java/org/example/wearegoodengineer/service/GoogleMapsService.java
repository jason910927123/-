package org.example.wearegoodengineer.service;

//import com.google.maps.GeoApiContext;
//import com.google.maps.PlacesApi;
//import com.google.maps.model.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import org.springframework.web.server.ResponseStatusException;
//
//import javax.annotation.PreDestroy;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;

//@Service
//public class GoogleMapsService {
//    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsService.class);
//    private static final int MAX_RETRIES = 3;
//    private static final String PHOTO_URL_TEMPLATE =
//            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=%s&key=%s";
//
//    private final GeoApiContext context;
//    private final String googleMapsApiKey;
//
//    public GoogleMapsService(@Value("${google.maps.api.key}") String apiKey) {
//        if (!StringUtils.hasText(apiKey)) {
//            throw new IllegalArgumentException("Google Maps API key cannot be empty");
//        }
//        this.googleMapsApiKey = apiKey;
//        this.context = new GeoApiContext.Builder()
//                .apiKey(apiKey)
//                .build();
//    }
//
//    public Map<String, Object> getPlaceDetailsByName(String placeName) {
//        validatePlaceName(placeName);
//
//        try {
//            logger.info("開始查詢地點詳情: {}", placeName);
//            String placeId = findPlaceId(placeName);
//            PlaceDetails placeDetails = getPlaceDetails(placeId);
//            return convertToResponseMap(placeName, placeDetails);
//        } catch (ResponseStatusException e) {
//            throw e;
//        } catch (Exception e) {
//            logger.error("獲取地點詳情時發生錯誤: ", e);
//            throw new ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR,
//                    String.format("獲取地點 '%s' 詳情失敗: %s", placeName, e.getMessage())
//            );
//        }
//    }
//
//    private void validatePlaceName(String placeName) {
//        if (!StringUtils.hasText(placeName)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "地點名稱不能為空");
//        }
//        if (placeName.length() > 500) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "地點名稱太長");
//        }
//    }
//
//    private String findPlaceId(String placeName) throws Exception {
//        FindPlaceFromTextRequest findPlaceRequest = PlacesApi.findPlaceFromText(
//                context,
//                placeName,
//                FindPlaceFromTextRequest.InputType.TEXT_QUERY
//        );
//
//        FindPlaceFromText findPlaceResponse = findPlaceRequest
//                .fields(FindPlaceFromTextRequest.FieldMask.PLACE_ID)
//                .await();
//
//        if (findPlaceResponse.candidates == null || findPlaceResponse.candidates.length == 0) {
//            logger.warn("找不到地點: {}", placeName);
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到指定的地點");
//        }
//
//        return findPlaceResponse.candidates[0].placeId;
//    }
//
//    private PlaceDetails getPlaceDetails(String placeId) throws Exception {
//        return PlacesApi.placeDetails(context, placeId)
//                .fields(
//                        PlaceDetailsRequest.FieldMask.FORMATTED_ADDRESS,
//                        PlaceDetailsRequest.FieldMask.FORMATTED_PHONE_NUMBER,
//                        PlaceDetailsRequest.FieldMask.OPENING_HOURS,
//                        PlaceDetailsRequest.FieldMask.PHOTOS,
//                        PlaceDetailsRequest.FieldMask.RATING,
//                        PlaceDetailsRequest.FieldMask.USER_RATINGS_TOTAL,
//                        PlaceDetailsRequest.FieldMask.WEBSITE,
//                        PlaceDetailsRequest.FieldMask.GEOMETRY
//                )
//                .await();
//    }
//
//    private Map<String, Object> convertToResponseMap(String placeName, PlaceDetails details) {
//        Map<String, Object> result = new HashMap<>();
//        result.put("name", placeName);
//        result.put("address", details.formattedAddress);
//        result.put("phone", details.formattedPhoneNumber);
//        result.put("rating", details.rating);
//        result.put("totalRatings", details.userRatingsTotal);
//        result.put("website", Optional.ofNullable(details.website)
//                .map(Object::toString)
//                .orElse(null));
//
//        result.put("openingHours", convertOpeningHours(details.openingHours));
//        result.put("isCurrentlyOpen", Optional.ofNullable(details.openingHours)
//                .map(hours -> hours.openNow)
//                .orElse(false));
//
//        result.put("photos", convertPhotos(details.photos));
//        result.put("location", convertLocation(details.geometry));
//
//        return result;
//    }
//
//    private List<Map<String, String>> convertOpeningHours(OpeningHours openingHours) {
//        List<Map<String, String>> result = new ArrayList<>();
//        if (openingHours != null && openingHours.weekdayText != null) {
//            for (String weekdayText : openingHours.weekdayText) {
//                String[] parts = weekdayText.split(": ", 2);
//                Map<String, String> dayHours = new HashMap<>();
//                dayHours.put("day", parts[0]);
//                dayHours.put("hours", parts.length > 1 ? parts[1] : "未提供");
//                result.add(dayHours);
//            }
//        }
//        return result;
//    }
//
//    private List<Map<String, String>> convertPhotos(PhotoReference[] photos) {
//        List<Map<String, String>> result = new ArrayList<>();
//        if (photos != null) {
//            for (PhotoReference photo : photos) {
//                if (photo.photoReference != null) {
//                    Map<String, String> photoInfo = new HashMap<>();
//                    photoInfo.put("reference", photo.photoReference);
//                    photoInfo.put("height", String.valueOf(photo.height));
//                    photoInfo.put("width", String.valueOf(photo.width));
//                    photoInfo.put("url", String.format(PHOTO_URL_TEMPLATE,
//                            photo.photoReference, googleMapsApiKey));
//                    result.add(photoInfo);
//                }
//            }
//        }
//        return result;
//    }
//
//    private Map<String, Double> convertLocation(Geometry geometry) {
//        Map<String, Double> location = new HashMap<>();
//        if (geometry != null && geometry.location != null) {
//            location.put("latitude", geometry.location.lat);
//            location.put("longitude", geometry.location.lng);
//        }
//        return location;
//    }
//
//    @PreDestroy
//    public void shutdown() {
//        if (context != null) {
//            try {
//                context.shutdown();
//            } catch (Exception e) {
//                logger.error("關閉 GeoApiContext 時發生錯誤", e);
//            }
//        }
//    }

//}