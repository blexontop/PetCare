package petcare.petcare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import petcare.petcare.model.User;
import petcare.petcare.repository.UserRepository;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication, HttpSession session) {
        boolean isGuest = false;
        if (session != null && session.getAttribute("isGuest") != null) {
            Object val = session.getAttribute("isGuest");
            if (val instanceof Boolean) {
                isGuest = (Boolean) val;
            }
        }

        model.addAttribute("isGuest", isGuest);

        User user = null;
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    user = userRepository.findByEmail(email).orElse(null);
                }
            } else if (principal instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                if (email != null) {
                    user = userRepository.findByEmail(email).orElse(null);
                }
            }
        }

        model.addAttribute("usuario", user);
    }
}
