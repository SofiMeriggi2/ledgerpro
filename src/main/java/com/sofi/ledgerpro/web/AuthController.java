package com.sofi.ledgerpro.web;

import com.sofi.ledgerpro.config.JwtService;
import com.sofi.ledgerpro.model.User;
import com.sofi.ledgerpro.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    // Constructor explícito (en lugar de @RequiredArgsConstructor)
    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    // DTO simple para el body
    public record Credentials(String email, String password) {}

    @PostMapping("/signup") // <-- corregido
    public ResponseEntity<?> signup(@RequestBody Credentials body) {
        if (users.findByEmail(body.email()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ese email ya está registrado"));
        }

        var u = new User();
        u.setEmail(body.email());
        u.setPassword(encoder.encode(body.password()));
        users.save(u);

        var token = jwt.issue(u.getId(), u.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials body) {
        var u = users.findByEmail(body.email()).orElse(null); // <-- sin "other:null"
        if (u == null || !encoder.matches(body.password(), u.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciales inválidas"));
        }

        var token = jwt.issue(u.getId(), u.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
