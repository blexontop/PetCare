package petcare.petcare.security;

import org.springframework.security.authorization.AuthorizationDecision;  // Importa la clase para decidir si la autorización es concedida o no
import org.springframework.security.authorization.AuthorizationManager;  // Importa la interfaz para gestionar la autorización
import org.springframework.security.core.Authentication;  // Importa la clase Authentication que contiene los detalles del usuario autenticado
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;  // Importa el contexto de autorización que contiene la solicitud HTTP
import jakarta.servlet.http.HttpServletRequest;  // Importa la clase HttpServletRequest para manejar la solicitud HTTP
import jakarta.servlet.http.HttpSession;  // Importa la clase HttpSession para acceder a la sesión del usuario
import java.util.function.Supplier;  // Importa Supplier para proporcionar un objeto de tipo Authentication de forma perezosa

// Clase que implementa AuthorizationManager para autorizar las solicitudes según el estado de autenticación
public class GuestOrAuthenticatedAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    // Método de autorización que decide si una solicitud es permitida
    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context) {
        
        HttpServletRequest request = context.getRequest();  // Obtiene la solicitud HTTP
        HttpSession session = request.getSession(false);  // Obtiene la sesión sin crear una nueva si no existe

        Authentication auth = authentication.get();  // Obtiene el objeto Authentication que contiene los detalles del usuario

        // Permite el acceso si el usuario está autenticado
        if (auth != null && auth.isAuthenticated()) {
            return new AuthorizationDecision(true);  // Decisión positiva: acceso permitido
        }

        // Permite el acceso si el usuario está marcado como invitado
        if (session != null && session.getAttribute("isGuest") != null) {
            Boolean isGuest = (Boolean) session.getAttribute("isGuest");  // Verifica si la sesión tiene el atributo "isGuest"
            if (isGuest) {
                return new AuthorizationDecision(true);  // Decisión positiva: acceso permitido para invitados
            }
        }

        // Si no está autenticado ni es invitado, el acceso es denegado
        return new AuthorizationDecision(false);  // Decisión negativa: acceso denegado
    }
}
