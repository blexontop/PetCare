package petcare.petcare.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.function.Supplier;

public class GuestOrAuthenticatedAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        HttpSession session = request.getSession(false);

        Authentication auth = authentication.get();

        // Permitir si está autenticado
        if (auth != null && auth.isAuthenticated()) {
            return new AuthorizationDecision(true);
        }

        // Permitir si es invitado
        if (session != null && session.getAttribute("isGuest") != null) {
            Boolean isGuest = (Boolean) session.getAttribute("isGuest");
            if (isGuest) {
                return new AuthorizationDecision(true);
            }
        }

        return new AuthorizationDecision(false);
    }
}
