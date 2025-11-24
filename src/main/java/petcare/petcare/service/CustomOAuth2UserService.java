package petcare.petcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import petcare.petcare.model.AuthProvider;
import petcare.petcare.model.Dueno;
import petcare.petcare.model.User;
import petcare.petcare.repository.DuenoRepository;
import petcare.petcare.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DuenoRepository duenoRepository;
    private final EmailService emailService;

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String login = (String) attributes.get("login");

        if (email == null && login != null) {
            email = login + "@github.local";
        }

        if (email != null) {
            Optional<User> existing = userRepository.findByEmail(email);
            User user;
            if (existing.isPresent()) {
                user = existing.get();
                user.setName(name != null ? name : user.getName());
                user.setPicture(picture != null ? picture : user.getPicture());
                user.setUpdatedAt(LocalDateTime.now());
            } else {
                AuthProvider provider = AuthProvider.GOOGLE;
                if (login != null || attributes.containsKey("id")) provider = AuthProvider.GITHUB;

                user = User.builder()
                        .email(email)
                        .name(name != null ? name : (login != null ? login : email))
                        .picture(picture)
                        .provider(provider)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                user.addRole("ROLE_USER");
                user = userRepository.save(user);

                // create Dueno minimal
                Dueno dueno = Dueno.builder()
                        .nombre(user.getName())
                        .email(user.getEmail())
                        .build();
                duenoRepository.save(dueno);

                // send welcome email async
                emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            }

            // Ensure user has at least ROLE_USER
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.addRole("ROLE_USER");
                userRepository.save(user);
            }
        }

        // Map authorities
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (oauth2User.getAuthorities() != null) {
            oauth2User.getAuthorities().forEach(a -> authorities.add(new SimpleGrantedAuthority(a.getAuthority())));
        }

        // If we have a persisted user, add their roles
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(u -> u.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority(r))));
        }

        return new DefaultOAuth2User(authorities, attributes, "email");
    }
}
