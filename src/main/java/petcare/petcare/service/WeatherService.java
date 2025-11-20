package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import petcare.petcare.dto.WeatherDto;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;

    public WeatherDto getCurrentWeather(String city) {
        // 1) Buscar lat/lon por nombre de ciudad
        String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + city +
                "&count=1&language=es&format=json";

        @SuppressWarnings("unchecked")
        Map<String, Object> geoResponse = (Map<String, Object>) restTemplate.getForObject(geoUrl, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results =
                (List<Map<String, Object>>) geoResponse.get("results");

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Ciudad no encontrada");
        }

        Map<String, Object> first = results.get(0);
        double lat = ((Number) first.get("latitude")).doubleValue();
        double lon = ((Number) first.get("longitude")).doubleValue();
        String resolvedName = (String) first.get("name");

        // 2) Obtener el tiempo actual
        String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                "&longitude=" + lon +
                "&current_weather=true";

        @SuppressWarnings("unchecked")
        Map<String, Object> weatherResponse = (Map<String, Object>) restTemplate.getForObject(weatherUrl, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> current =
                (Map<String, Object>) weatherResponse.get("current_weather");

        double temperature = ((Number) current.get("temperature")).doubleValue();
        double windspeed = ((Number) current.get("windspeed")).doubleValue();

        WeatherDto dto = new WeatherDto();
        dto.setCity(resolvedName);
        dto.setLatitude(lat);
        dto.setLongitude(lon);
        dto.setTemperature(temperature);
        dto.setWindspeed(windspeed);

        return dto;
    }
}
