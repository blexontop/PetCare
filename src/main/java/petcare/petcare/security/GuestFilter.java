package petcare.petcare.security;

import jakarta.servlet.Filter;  
import jakarta.servlet.FilterChain;  
import jakarta.servlet.ServletException; 
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;  
import jakarta.servlet.http.HttpServletRequest; 
import jakarta.servlet.http.HttpSession;  
import org.springframework.stereotype.Component;  

import java.io.IOException;  // Excepción relacionada con errores de entrada/salida

@Component  // Marca la clase como un componente de Spring para ser gestionado por el contenedor de Spring
public class GuestFilter implements Filter {

    // Método que filtra las solicitudes HTTP antes de que lleguen a su destino
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;  // Convierte la solicitud en una solicitud HTTP
        HttpSession session = httpRequest.getSession(false);  // Obtiene la sesión actual (sin crear una nueva)

        // Verifica si la sesión tiene el atributo "isGuest"
        if (session != null && session.getAttribute("isGuest") != null) {
            Boolean isGuest = (Boolean) session.getAttribute("isGuest");  // Obtiene el valor del atributo "isGuest"

            // Si es un invitado y está intentando acceder al mapa, permitir el acceso
            if (isGuest && httpRequest.getRequestURI().equals("/mapa")) {
                // Permite que la solicitud continúe hacia su destino
                chain.doFilter(request, response);
                return;  // Termina la ejecución aquí para no pasar por el filtro de nuevo
            }
        }

        // Si no es un invitado o no está accediendo al mapa, la solicitud continúa normalmente
        chain.doFilter(request, response);
    }
}
