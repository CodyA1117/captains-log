package com.minderall.captainslogapp.Controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

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

    // 2. Handle the callback and exchange the code for a token
    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam String code) {
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

        // For now, just return the token JSON so we can test
        return ResponseEntity.ok(response.getBody());
    }
}
