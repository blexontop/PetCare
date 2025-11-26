package petcare.petcare.security;

import jakarta.servlet.ServletException;  // Importa la clase ServletException para manejar excepciones relacionadas con servlets
import jakarta.servlet.http.HttpServletRequest;  // Importa la clase HttpServletRequest para manejar la solicitud HTTP
import jakarta.servlet.http.HttpServletResponse;  // Importa la clase HttpServletResponse para manejar la respuesta HTTP
import jakarta.servlet.http.HttpSession;  // Importa la clase HttpSession para acceder a la sesión HTTP
import org.springframework.security.core.Authentication;  // Importa la clase Authentication para obtener detalles del usuario autenticado
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;  // Interfaz para manejar el éxito de la autenticación
import org.springframework.stereotype.Component;  // Anotación que marca esta clase como un componente de Spring

import java.io.IOException;  // Importa IOException para manejar errores de entrada/salida

@Component  // Marca esta clase como un componente gestionado por Spring
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final String defaultTargetUrl = "/dashboard";  // URL de redirección después de un login exitoso

    // Método que se ejecuta después de una autenticación exitosa
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        HttpSession session = request.getSession(false);  // Obtiene la sesión HTTP sin crear una nueva si no existe
        
        // Si la sesión existe y contiene el atributo "isGuest", lo elimina
        if (session != null && session.getAttribute("isGuest") != null) {
            session.removeAttribute("isGuest");  // Elimina el atributo "isGuest" de la sesión
        }

        // Redirige al usuario a la URL predeterminada después de un login exitoso
        response.sendRedirect(request.getContextPath() + defaultTargetUrl);  // Redirige al dashboard
    }
}
