package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MapaService {

    private final RestTemplate restTemplate;

    // -------------------------------------------------------------------------
// 1. GEOCODIFICACIÓN: geocodificarDireccion(String direccion)
// -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Map<String, Object> geocodificarDireccion(String direccion) {

        URI uri = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("q", direccion)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .build()
                .toUri();

        Map<String, Object>[] response = (Map<String, Object>[]) restTemplate.getForObject(uri, Map[].class);

        if (response != null && response.length > 0) {
            return response[0];
        }

        return null; // No encontrado
    }

    // -------------------------------------------------------------------------
// 2. SOLO COORDENADAS: obtenerCoordenadas(String direccion)
// -------------------------------------------------------------------------
    public Double[] obtenerCoordenadas(String direccion) {

        Map<String, Object> response = geocodificarDireccion(direccion);

        if (response != null) {
            Double lat = Double.parseDouble((String) response.get("lat"));
            Double lon = Double.parseDouble((String) response.get("lon"));
            return new Double[]{lat, lon};
        }

        return null; // No encontrado
    }


    // -------------------------------------------------------------------------
// 3. RUTAS: obtenerRuta(double lat1, double lon1, double lat2, double lon2)
// -------------------------------------------------------------------------
    public String obtenerRuta(double lat1, double lon1, double lat2, double lon2) {
        String url = "http://router.project-osrm.org/route/v1/driving/"
                + lon1 + "," + lat1 + ";"
                + lon2 + "," + lat2
                + "?overview=full&geometries=geojson";

        return restTemplate.getForObject(url, String.class);
    }


    // -------------------------------------------------------------------------
// 4. DISTANCIA HAVERSINE: calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2)
// -------------------------------------------------------------------------
    public Double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2)
                * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
