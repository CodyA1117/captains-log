package com.minderall.captainslogapp.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.UserRepository;
import com.minderall.captainslogapp.Security.JwtUtil;
import com.minderall.captainslogapp.Services.OuraDataService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/oura")
public class OuraOAuthController {

    private static final Logger logger = LoggerFactory.getLogger(OuraOAuthController.class); // Added logger

    @Value("${oura.client.id}")
    private String clientId;

    @Value("${oura.client.secret}")
    private String clientSecret;

    @Value("${oura.redirect.uri}")
    private String redirectUri; // This is the redirect_uri registered with Oura

    @Value("${oura.auth.uri}")
    private String authUri;

    @Value("${oura.token.uri}")
    private String tokenUri;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OuraTokenRepository ouraTokenRepository;

    @Autowired
    private OuraDataService ouraDataService;

    @Autowired
    private JwtUtil jwtUtil;


    //Triggers fetch manually for testing.
    @GetMapping("/fetch-readiness")
    public ResponseEntity<String> fetchTodayReadiness(Principal principal) {
        // Ensure principal is not null and getName() returns the email
        if (principal == null || principal.getName() == null) {
            logger.warn("/fetch-readiness called without authenticated principal.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        ouraDataService.fetchAndSaveTodayReadiness(principal.getName());
        return ResponseEntity.ok("Readiness data fetched and saved.");
    }

    // 1. Start the Oura OAuth process
    @GetMapping("/start")
    public void startOAuth(HttpServletResponse response) throws IOException {
        // This redirectUri is the one registered with Oura.
        // It's where Oura will send the user (and the code) back AFTER they authorize.
        // Based on your frontend code, this should be your frontend URL (e.g., http://localhost:3000/ourasuccess)
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String url = authUri +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + encodedRedirectUri +
                "&scope=email+personal+daily"; // Ensure 'daily' scope is sufficient for your data needs
        logger.info("Redirecting to Oura for authorization: {}", url);
        response.sendRedirect(url);
    }

    // 2. Handle the callback (This method is likely NOT used if your redirect_uri points to the frontend)
    // If Oura's redirect_uri was set to point to THIS backend endpoint, this would be used.
    // The @AuthenticationPrincipal UserDetails here implies the user must already be logged into your app
    // and the session persists across the Oura redirect.
    @GetMapping("/callback") // Changed to GET as Oura usually callbacks with GET, and it's often public
    public void handleOuraCallback( // Renamed for clarity, changed return to void for direct redirect
                                    @RequestParam String code,
                                    // @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails, // This might be problematic if session doesn't persist well
                                    HttpServletResponse httpServletResponse // For redirecting the user's browser
    ) throws IOException {
        logger.info("Received Oura callback with code (this endpoint might be unused in current flow): {}", code);

        // >> IF THIS /callback ENDPOINT IS INDEED YOUR OURARING.COM REDIRECT_URI: <<
        // You need a way to associate this callback with the user who initiated it.
        // Common ways:
        // 1. Use the 'state' OAuth parameter: Generate a unique state in /start, store it (e.g., in session or temp DB mapped to user),
        //    Oura returns it, you verify it and retrieve the user.
        // 2. If @AuthenticationPrincipal works reliably across redirects, then use userDetails.
        // For now, let's assume this /callback might be the redirect_uri and it needs to save the token
        // and then redirect the user's BROWSER to the frontend dashboard.

        // WARNING: The AuthenticationPrincipal might be null here if the session isn't properly
        // maintained across the external redirect to Oura and back.
        // A robust solution often involves a 'state' parameter.
        // For demonstration, proceeding as if userDetails could be available or you'd fetch user via 'state'.
        // String userEmail = userDetails != null ? userDetails.getUsername() : null;
        // if (userEmail == null) {
        //     logger.error("User email could not be determined in /callback. Cannot save Oura token.");
        //     httpServletResponse.sendRedirect("/login?error=oura_auth_failed"); // Redirect to login or error page
        //     return;
        // }

        // The rest of this method would be similar to /save-token's token exchange and saving logic.
        // After saving, you would redirect the BROWSER:
        // httpServletResponse.sendRedirect("http://localhost:3000/dashboard"); // Or your frontend dashboard URL
        // For now, since the primary error is with /save-token, I'm commenting out the active logic here
        // to avoid confusion. If this IS your redirect URI, uncomment and adapt.

        logger.warn("/api/oura/callback hit. If your Oura App redirect_uri points to your frontend, this backend endpoint is likely not used in that flow.");
        httpServletResponse.sendRedirect("http://localhost:3000/dashboard?message=callback_hit_unexpectedly"); // Placeholder redirect
    }


    // This is the endpoint your OuraSuccess.js calls after Oura redirects to it with a code.
    @PostMapping("/save-token")
    public ResponseEntity<String> saveToken(
            @RequestBody Map<String, String> payload, // Changed from 'body' to 'payload' for clarity
            @RequestHeader("Authorization") String authHeader
    ) {
        String code = payload.get("code");
        logger.info("🔐 /api/oura/save-token called with code: {}", code != null ? code.substring(0, Math.min(code.length(), 10))+"..." : "null"); // Log part of code

        if (code == null || code.trim().isEmpty()) {
            logger.warn("Received empty or null code in /save-token request.");
            return ResponseEntity.badRequest().body("Authorization code must be provided.");
        }

        try {
            String jwt = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(jwt);

            if (!jwtUtil.validateToken(jwt, email)) { // Validate JWT
                logger.warn("Invalid JWT received for email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired JWT.");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("User not found for email from JWT: {}", email);
                        return new UsernameNotFoundException("User not found: " + email);
                    });

            logger.info("Attempting to exchange Oura code for user: {}", email);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("code", code);
            formData.add("redirect_uri", redirectUri); // Must match the redirect_uri used in /start and registered with Oura
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
            ResponseEntity<String> ouraResponse;

            try {
                ouraResponse = restTemplate.postForEntity(tokenUri, requestEntity, String.class);
            } catch (HttpClientErrorException e) {
                logger.error("Oura API Error during token exchange for user {}: {} - {}", email, e.getStatusCode(), e.getResponseBodyAsString(), e);
                return ResponseEntity.status(e.getStatusCode()).body("Failed to retrieve token from Oura: " + e.getResponseBodyAsString());
            }

            if (ouraResponse.getStatusCode().is2xxSuccessful() && ouraResponse.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(ouraResponse.getBody());

                String accessToken = json.path("access_token").asText(null);
                String refreshToken = json.path("refresh_token").asText(null);
                int expiresIn = json.path("expires_in").asInt(-1);

                if (accessToken == null || refreshToken == null || expiresIn == -1) {
                    logger.error("Incomplete token data from Oura for user {}: {}", email, ouraResponse.getBody());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Received incomplete token data from Oura.");
                }

                LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

                // Upsert logic for OuraToken
                OuraToken tokenToSave = ouraTokenRepository.findByUser(user) // Assuming OuraTokenRepository has findByUser(User user)
                        .orElse(new OuraToken()); // If not found, create a new one

                tokenToSave.setUser(user);
                tokenToSave.setAccessToken(accessToken);
                tokenToSave.setRefreshToken(refreshToken);
                tokenToSave.setExpiresAt(expiresAt);
                // Set any other relevant fields if your OuraToken has them

                ouraTokenRepository.save(tokenToSave);
                logger.info("Oura token saved/updated successfully for user: {}", email);

                // Trigger initial data sync
                logger.info("Triggering initial data sync for user: {}", email);
                ouraDataService.fetchAndSaveTodayReadiness(email);
                ouraDataService.fetchAndSaveTodaySleepScore(email);
                ouraDataService.fetchAndSaveTodayActivityScore(email);
                ouraDataService.fetchAndSaveTodayHeartRate(email);
                // Add other syncs if necessary

                return ResponseEntity.ok("Oura token saved and initial data sync initiated.");
            } else {
                logger.error("Failed to retrieve token from Oura for user {}. Status: {}, Body: {}", email, ouraResponse.getStatusCode(), ouraResponse.getBody());
                return ResponseEntity.status(ouraResponse.getStatusCode()).body("Failed to retrieve token from Oura. Response: " + ouraResponse.getBody());
            }
        } catch (UsernameNotFoundException e) {
            // Already logged, rethrow or handle as client error
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Exception in /save-token for code {}: {}", code != null ? code.substring(0, Math.min(code.length(), 10))+"..." : "null", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}