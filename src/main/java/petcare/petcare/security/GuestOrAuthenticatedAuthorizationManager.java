package petcare.petcare.security;

import org.springframework.security.authorization.AuthorizationDecision;  
import org.springframework.security.authorization.AuthorizationManager;  
import org.springframework.security.core.Authentication; 
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;  
import jakarta.servlet.http.HttpServletRequest; 
import jakarta.servlet.http.HttpSession;  
import java.util.function.Supplier; 

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
