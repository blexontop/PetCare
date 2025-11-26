package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service                                      // Indica que esta clase es un servicio de Spring
@RequiredArgsConstructor                      // Genera un constructor con los campos "final"
public class MapaService {

    private final RestTemplate restTemplate;   // RestTemplate inyectado para realizar peticiones HTTP

    // -------------------------------------------------------------------------
    // 1. GEOCODIFICACIÓN: geocodificarDireccion(String direccion)
    // Convierte una dirección en datos JSON que incluyen latitud y longitud
    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Map<String, Object> geocodificarDireccion(String direccion) {

        // Construimos la URL para llamar al servicio Nominatim de OpenStreetMap
        URI uri = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/search")
                .queryParam("q", direccion)       // Dirección ingresada
                .queryParam("format", "json")      // Respuesta JSON
                .queryParam("limit", "1")          // Solo un resultado
                .build()
                .toUri();

        // Realizamos la petición y recibimos un arreglo de Map
        Map<String, Object>[] response = (Map<String, Object>[]) restTemplate.getForObject(uri, Map[].class);

        // Si hay resultados, retornamos el primer elemento
        if (response != null && response.length > 0) {
            return response[0];
        }

        return null; // No encontrado
    }

    // -------------------------------------------------------------------------
    // 2. SOLO COORDENADAS: obtenerCoordenadas(String direccion)
    // Retorna únicamente latitud y longitud como arreglo de Double
    // -------------------------------------------------------------------------
    public Double[] obtenerCoordenadas(String direccion) {

        // Obtenemos el resultado de geocodificación
        Map<String, Object> response = geocodificarDireccion(direccion);

        // Si existe, extraemos lat y lon de la respuesta
        if (response != null) {
            Double lat = Double.parseDouble((String) response.get("lat"));
            Double lon = Double.parseDouble((String) response.get("lon"));
            return new Double[]{lat, lon};
        }

        return null; // No encontrado
    }


    // -------------------------------------------------------------------------
    // 3. RUTAS: obtenerRuta(double lat1, double lon1, double lat2, double lon2)
    // Obtiene la ruta en formato GeoJSON entre dos coordenadas usando OSRM
    // -------------------------------------------------------------------------
    public String obtenerRuta(double lat1, double lon1, double lat2, double lon2) {

        // Construimos la URL para OSRM (usa formato lon,lat)
        String url = "http://router.project-osrm.org/route/v1/driving/"
                + lon1 + "," + lat1 + ";"   // Punto inicial
                + lon2 + "," + lat2         // Punto final
                + "?overview=full&geometries=geojson"; // Ruta completa en GeoJSON

        // Hacemos la petición y retornamos la respuesta como String JSON
        return restTemplate.getForObject(url, String.class);
    }


    // -------------------------------------------------------------------------
    // 4. DISTANCIA HAVERSINE: calcularDistanciaKm(...)
    // Calcula la distancia en línea recta entre dos coordenadas usando la fórmula Haversine
    // -------------------------------------------------------------------------
    public Double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {

        final int R = 6371; // Radio de la Tierra en km

        // Diferencias de latitud y longitud en radianes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Convertimos latitudes originales a radianes
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Aplicamos la fórmula de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2)
                * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Devolvemos la distancia total en kilómetros
        return R * c;
    }
}
