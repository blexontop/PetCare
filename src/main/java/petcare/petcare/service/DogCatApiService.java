package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio para interactuar con APIs externas que proporcionan imágenes
 * aleatorias de perros y gatos.
 * Utiliza RestTemplate para realizar las llamadas HTTP.
 */
@Service
@RequiredArgsConstructor
public class DogCatApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Llama a la API de 'dog.ceo' para obtener una URL de una imagen de perro
     * aleatoria.
     * 
     * @return Una cadena con la URL de la imagen si la llamada es exitosa, o null
     *         en caso contrario.
     */
    public String obtenerImagenPerroAleatoria() {
        String url = "https://dog.ceo/api/breeds/image/random";
        // Realiza una petición GET y mapea (clave-valor) la respuesta JSON a un mapa.
        Map<?, ?> respuesta = restTemplate.getForObject(url, Map.class);
        // Verifica que la respuesta sea exitosa y contenga el campo 'message' con la
        // URL.
        if (respuesta != null && "success".equals(respuesta.get("status"))) {
            // La API incluye un campo "status". Se verifica que exista una respuesta y que
            // el estado sea "success".
            Object message = respuesta.get("message"); // Se coje el valor de la respuesta.
            return message != null ? message.toString() : null; // Si el valor no es nulo, se convierte a cadena y se
                                                                // retorna.
        }
        return null; // Retorna null si la API falla o la respuesta no es la esperada.
    }

    /**
     * Llama a la API de 'thecatapi.com' para obtener una URL de una imagen de gato
     * aleatoria.
     * 
     * @return Una cadena con la URL de la imagen si la llamada es exitosa, o null
     *         en caso contrario.
     */
    public String obtenerImagenGatoAleatoria() {
        String url = "https://api.thecatapi.com/v1/images/search";
        // La respuesta es un array JSON, por lo que se mapea a un array de objetos.
        Object[] respuesta = restTemplate.getForObject(url, Object[].class);
        // Verifica que la respuesta (un array JSON) no sea nula, contenga al menos un
        // elemento y que este sea un mapa.
        if (respuesta != null && respuesta.length > 0 && respuesta[0] instanceof Map) {
            // Extrae la URL del mapa.
            Object urlImg = ((Map<?, ?>) respuesta[0]).get("url");
            // Se accede al primer elemento del array
            // (respuesta[0]), se trata como un mapa, y se extrae
            // el valor de la clave "url", que es la URL de la
            // imagen, pasándola a cadena si no es nula.
            return urlImg != null ? urlImg.toString() : null;
        }
        return null; // Retorna null si la respuesta no es válida.
    }
}
