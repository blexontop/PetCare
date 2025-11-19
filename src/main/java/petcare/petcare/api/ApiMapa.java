package petcare.petcare.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/mapa")
@CrossOrigin(origins = "*")
public class ApiMapa {

    private final RestTemplate restTemplate = new RestTemplate();

    // -------------------------------------------------------------------------
    // 1. GEOCODIFICACIÓN: /api/mapa/geocodificar?direccion=...
    // -------------------------------------------------------------------------
    @GetMapping("/geocodificar")
    public Map[] geocodificarDireccion(@RequestParam String direccion) {

        URI uri = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("q", direccion)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .build()
                .toUri();

        return restTemplate.getForObject(uri, Map[].class);
    }

    // -------------------------------------------------------------------------
    // 2. SOLO COORDENADAS: /api/mapa/coordenadas?direccion=...
    // -------------------------------------------------------------------------
    @GetMapping("/coordenadas")
    public Double[] obtenerCoordenadas(@RequestParam String direccion) {

        Map[] response = geocodificarDireccion(direccion);

        if (response != null && response.length > 0) {
            Double lat = Double.parseDouble((String) response[0].get("lat"));
            Double lon = Double.parseDouble((String) response[0].get("lon"));
            return new Double[]{lat, lon};
        }

        return null; // No encontrado
    }


    // -------------------------------------------------------------------------
    // 3. RUTAS: /api/mapa/ruta?lat1=&lon1=&lat2=&lon2=
    // -------------------------------------------------------------------------
    @GetMapping("/ruta")
    public String obtenerRuta(
            @RequestParam Double lat1,
            @RequestParam Double lon1,
            @RequestParam Double lat2,
            @RequestParam Double lon2
    ) {
        String url = "http://router.project-osrm.org/route/v1/driving/"
                + lon1 + "," + lat1 + ";"
                + lon2 + "," + lat2
                + "?overview=full&geometries=geojson";

        return restTemplate.getForObject(url, String.class);
    }


    // -------------------------------------------------------------------------
    // 4. DISTANCIA HAVERSINE: /api/mapa/distancia?lat1=&lon1=&lat2=&lon2=
    // -------------------------------------------------------------------------
    @GetMapping("/distancia")
    public Double calcularDistanciaKm(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2
    ) {
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
