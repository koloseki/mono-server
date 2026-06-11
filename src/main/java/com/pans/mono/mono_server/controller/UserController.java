package com.pans.mono.mono_server.controller;

import com.pans.mono.mono_server.dto.AuthResponse;
import com.pans.mono.mono_server.model.User;
import com.pans.mono.mono_server.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration and login")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static class AuthRequest {
        public String username;
        public String password;
    }

    @Operation(summary = "Register a new user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Username already taken or password too short")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        if (request.password == null || request.password.length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
        }

        if (userRepository.existsByUsername(request.username)) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        String hashedPw = BCrypt.hashpw(request.password, BCrypt.gensalt());

        User newUser = new User();
        newUser.setUsername(request.username);
        newUser.setPassword(hashedPw);
        userRepository.save(newUser);

        return ResponseEntity.ok("Registration successful");
    }

    @Operation(summary = "Login and receive a session token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Returns sessionToken"),
        @ApiResponse(responseCode = "400", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        var userOptional = userRepository.findByUsername(request.username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        User user = userOptional.get();

        if (!BCrypt.checkpw(request.password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }else {
            String sessionToken = UUID.randomUUID().toString();
            user.setSessionToken(sessionToken);
            userRepository.save(user);

            return ResponseEntity.ok(new AuthResponse(sessionToken));
        }
    }
}
