package com.pans.mono.mono_server.controller;

import com.pans.mono.mono_server.model.User;
import com.pans.mono.mono_server.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static class AuthRequest {
        public String username;
        public String password;
    }

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

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        var userOptional = userRepository.findByUsername(request.username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOptional.get();

        if (!BCrypt.checkpw(request.password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }else {
            return ResponseEntity.ok("You are logged in as: " + user.getUsername() + " :)");
        }
    }
}
