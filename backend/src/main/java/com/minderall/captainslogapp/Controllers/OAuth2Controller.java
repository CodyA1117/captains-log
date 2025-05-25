package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.Security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@Controller
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public OAuth2Controller(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    // This endpoint initiates the Oura connection for the currently logged-in user
    @GetMapping("/connect/{registrationId}")
    @PreAuthorize("hasRole('USER')") // Ensure user is logged into our app first
    public RedirectView initiateOuraConnection(@PathVariable String registrationId,
                                               HttpServletRequest request,
                                               HttpServletResponse response,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        if (userDetails == null) {
            // Should be caught by PreAuthorize, but good to check
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User must be logged in to connect Oura account.");
            return null;
        }

        // The registrationId should be "oura"
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown OAuth2 registration ID: " + registrationId);
            return null;
        }

        // Construct the authorization request URI
        // Spring Security's OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        // is "/oauth2/authorization". So the redirect will go to /oauth2/authorization/oura
        // This path is handled by Spring Security to then redirect to Oura's actual auth URI.
        String redirectUrl = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + registrationId;
        return new RedirectView(redirectUrl);
    }
}