package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import petcare.petcare.dto.WeatherDto;
import petcare.petcare.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    // GET http://localhost:8080/api/weather/current?city=Cadiz
    @GetMapping("/current")
    public ResponseEntity<WeatherDto> getCurrentWeather(@RequestParam String city) {
        WeatherDto dto = weatherService.getCurrentWeather(city);
        return ResponseEntity.ok(dto);
    }
}
