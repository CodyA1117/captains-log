package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.UserRepository;
import com.minderall.captainslogapp.Security.JwtUtil;
import com.minderall.captainslogapp.dto.AuthenticationRequestDTO;
import com.minderall.captainslogapp.dto.AuthenticationResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/test/admin-only")
    public String adminOnlyRoute() {
        return "Only accessible by users with ADMIN role!";
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(@RequestBody AuthenticationRequestDTO request) {
        System.out.println("📨 Received registration request for: " + request.getEmail());
        System.out.println("🔑 Raw password input: " + request.getPassword());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole("USER"); // default role
        userRepository.save(newUser);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String email = authentication.getName(); // This is the username/email
        String token = jwtUtil.generateToken(email);

        return ResponseEntity.ok(new AuthenticationResponseDTO(token, newUser.getEmail(), newUser.getRole(), newUser.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody AuthenticationRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String email = authentication.getName(); // This is the username/email
        String token = jwtUtil.generateToken(email);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return ResponseEntity.ok(new AuthenticationResponseDTO(token, user.getEmail(), user.getRole(), user.getId()));
    }


}
