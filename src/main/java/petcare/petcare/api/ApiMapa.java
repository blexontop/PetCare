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

    // Usamos RestTemplate para realizar las peticiones HTTP externas
    private final RestTemplate restTemplate = new RestTemplate();

    // -------------------------------------------------------------------------
    // 1. GEOCODIFICACIÓN: Convierte una dirección en coordenadas (lat, lon)
    // Endpoint: /api/mapa/geocodificar?direccion=...
    // -------------------------------------------------------------------------
    @GetMapping("/geocodificar")
    public Map<String, Object>[] geocodificarDireccion(@RequestParam String direccion) {

        // Construimos la URL de consulta a Nominatim (OpenStreetMap)
        URI uri = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("q", direccion)       // dirección a buscar
                .queryParam("format", "json")      // formato de salida
                .queryParam("limit", "1")          // solo 1 resultado
                .build()
                .toUri();

        // Realizamos la petición REST y convertimos la respuesta en arreglo de mapas
        @SuppressWarnings("unchecked")
        Map<String, Object>[] response = (Map<String, Object>[]) restTemplate.getForObject(uri, Map[].class);
        return response;
    }

    // -------------------------------------------------------------------------
    // 2. SOLO COORDENADAS: Extrae latitud y longitud únicamente
    // Endpoint: /api/mapa/coordenadas?direccion=...
    // -------------------------------------------------------------------------
    @GetMapping("/coordenadas")
    public Double[] obtenerCoordenadas(@RequestParam String direccion) {

        // Reutilizamos la función anterior
        Map<String, Object>[] response = geocodificarDireccion(direccion);

        // Si hay resultados, convertimos lat/lon de String a Double
        if (response != null && response.length > 0) {
            Double lat = Double.parseDouble((String) response[0].get("lat"));
            Double lon = Double.parseDouble((String) response[0].get("lon"));
            return new Double[]{lat, lon};
        }

        // Si no se encuentra la dirección retornamos null
        return null;
    }


    // -------------------------------------------------------------------------
    // 3. RUTAS: Devuelve la ruta entre dos coordenadas usando OSRM
    // Endpoint: /api/mapa/ruta?lat1=&lon1=&lat2=&lon2=
    // -------------------------------------------------------------------------
    @GetMapping("/ruta")
    public String obtenerRuta(
            @RequestParam Double lat1,
            @RequestParam Double lon1,
            @RequestParam Double lat2,
            @RequestParam Double lon2
    ) {
        // Construimos la URL para OSRM Routing API
        String url = "http://router.project-osrm.org/route/v1/driving/"
                + lon1 + "," + lat1 + ";"   // punto inicial (OSRM usa lon,lat)
                + lon2 + "," + lat2         // punto final
                + "?overview=full&geometries=geojson"; // formato de la ruta

        // Retornamos el JSON de la ruta
        return restTemplate.getForObject(url, String.class);
    }


    // -------------------------------------------------------------------------
    // 4. DISTANCIA HAVERSINE: Calcula distancia entre dos puntos en línea recta
    // Endpoint: /api/mapa/distancia?lat1=&lon1=&lat2=&lon2=
    // -------------------------------------------------------------------------
    @GetMapping("/distancia")
    public Double calcularDistanciaKm(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2
    ) {
        final int R = 6371; // Radio de la Tierra en km

        // Convertimos diferencias a radianes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Convertimos latitudes originales a radianes
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Fórmula de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2)
                * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Retornamos distancia en km
        return R * c;
    }

}
