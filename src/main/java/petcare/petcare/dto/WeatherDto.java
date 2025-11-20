package petcare.petcare.dto;

import lombok.Data;

@Data
public class WeatherDto {
    private String city;
    private double latitude;
    private double longitude;
    private double temperature;
    private double windspeed;
}
