package com.minderall.captainslogapp.Security;

import com.minderall.captainslogapp.Services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;

@Component
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private UserService userService;

    @Value("${oura.redirect.uri}") // Your frontend success page
    private String frontendSuccessRedirectUri;

    @Value("${captainslogapp.app.frontendErrorRedirectUri:/oura-error}") // A default error page on frontend
    private String frontendErrorRedirectUri;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("OAuth2 Authentication successful. Processing tokens...");

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal(); // Contains attributes and potentially tokens depending on provider config

        // Get the currently logged-in Captains Log user details from the main security context
        // This assumes the user was already logged into our app before starting Oura connect
        Authentication captainLogAuthentication = SecurityContextHolder.getContext().getAuthentication();

        if (captainLogAuthentication == null || !(captainLogAuthentication.getPrincipal() instanceof UserDetailsImpl)) {
            logger.error("Captains Log user not found in SecurityContext. Cannot link Oura account.");
            getRedirectStrategy().sendRedirect(request, response, frontendErrorRedirectUri + "?error=UserNotLoggedIn");
            return;
        }

        UserDetailsImpl captainLogUserDetails = (UserDetailsImpl) captainLogAuthentication.getPrincipal();
        Long captainLogUserId = captainLogUserDetails.getId();

        // Extract Oura tokens
        // How tokens are accessed can depend on the client registration and provider.
        // Typically, for "authorization_code", Spring Security handles token storage internally
        // and they might not be directly on OAuth2User. We need to fetch them from the OAuth2AuthorizedClientService.
        // However, since this success handler is part of the OAuth2 flow, the OAuth2AuthenticationToken might give us what we need.
        // Let's refine token extraction if this isn't sufficient.
        // For now, we'll assume the necessary details can be derived or we'll enhance this.

        // The OAuth2AuthorizedClient contains the actual tokens.
        // We need to get it from OAuth2AuthorizedClientService.
        // This part needs to be handled carefully. The success handler might be too late to easily grab
        // the OAuth2AuthorizedClient for the *just completed* flow if it's already been "saved".

        // A common pattern is to use OAuth2AuthorizedClientService within this handler.
        // The principal of OAuth2AuthenticationToken IS the OAuth2User.
        // The OAuth2AuthenticationToken itself has a reference to the OAuth2AuthorizedClient.

        String accessToken = oauthToken.getAuthorizedClient().getAccessToken().getTokenValue();
        String refreshToken = null;
        if (oauthToken.getAuthorizedClient().getRefreshToken() != null) {
            refreshToken = oauthToken.getAuthorizedClient().getRefreshToken().getTokenValue();
        }

        Long expiresIn = null;
        Instant expiresAt = oauthToken.getAuthorizedClient().getAccessToken().getExpiresAt();
        if (expiresAt != null) {
            expiresIn = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        }


        if (accessToken == null) {
            logger.error("Oura access token not found for user ID: {}", captainLogUserId);
            getRedirectStrategy().sendRedirect(request, response, frontendErrorRedirectUri + "?error=TokenNotFound");
            return;
        }

        try {
            userService.saveOuraTokens(captainLogUserId, accessToken, refreshToken, expiresIn);
            logger.info("Successfully saved Oura tokens for Captains Log User ID: {}", captainLogUserId);

            String targetUrl = UriComponentsBuilder.fromUriString(frontendSuccessRedirectUri)
                    .queryParam("status", "success")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            logger.error("Error saving Oura tokens for Captains Log User ID: {}. Error: {}", captainLogUserId, e.getMessage(), e);
            String targetUrl = UriComponentsBuilder.fromUriString(frontendErrorRedirectUri)
                    .queryParam("error", "TokenSaveFailed")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}