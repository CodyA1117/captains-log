package com.minderall.captainslogapp.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.Models.OuraToken;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.OuraTokenRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/oura")
public class OuraOAuthController {

    @Value("${oura.client.id}")
    private String clientId;

    @Value("${oura.client.secret}")
    private String clientSecret;

    @Value("${oura.redirect.uri}")
    private String redirectUri;

    @Value("${oura.auth.uri}")
    private String authUri;

    @Value("${oura.token.uri}")
    private String tokenUri;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OuraTokenRepository ouraTokenRepository;

    // 1. Start the Oura OAuth process
    @GetMapping("/start")
    public void startOAuth(HttpServletResponse response) throws IOException {
        String url = authUri +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=email+personal+daily";
        response.sendRedirect(url);
    }

    // 2. Handle the callback and save the token
    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam String code, Principal principal) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());

            String accessToken = json.get("access_token").asText();
            String refreshToken = json.get("refresh_token").asText();
            int expiresIn = json.get("expires_in").asInt();
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

            String userEmail = principal.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

            OuraToken token = new OuraToken();
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(expiresAt);
            token.setUser(user);

            ouraTokenRepository.save(token);

            return ResponseEntity.ok("Oura token saved successfully.");
        }

        return ResponseEntity.status(response.getStatusCode()).body("Failed to retrieve token from Oura.");
    }

}
