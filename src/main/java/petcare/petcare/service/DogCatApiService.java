package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DogCatApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String obtenerImagenPerroAleatoria() {
        String url = "https://dog.ceo/api/breeds/image/random";
        Map<?,?> respuesta = restTemplate.getForObject(url, Map.class);
        if (respuesta != null && "success".equals(respuesta.get("status"))) {
            Object message = respuesta.get("message");
            return message != null ? message.toString() : null;
        }
        return null;
    }

    public String obtenerImagenGatoAleatoria() {
        String url = "https://api.thecatapi.com/v1/images/search";
        Object[] respuesta = restTemplate.getForObject(url, Object[].class);
        if (respuesta != null && respuesta.length > 0 && respuesta[0] instanceof Map) {
            Object urlImg = ((Map<?,?>) respuesta[0]).get("url");
            return urlImg != null ? urlImg.toString() : null;
        }
        return null;
    }
}
