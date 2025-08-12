package tv.oxnu0xuu.hachimiviewer.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final String reviewerPassword;
    private final String adminPassword;

    @Autowired
    public AuthController(@Qualifier("reviewerPassword") String reviewerPassword,
                          @Qualifier("adminPassword") String adminPassword) {
        this.reviewerPassword = reviewerPassword;
        this.adminPassword = adminPassword;
        System.out.println("AuthController initialized with admin password: " + (adminPassword != null ? "SET" : "NULL"));
    }

    @PostMapping("/reviewer/login")
    public ResponseEntity<?> reviewerLogin(@RequestBody Map<String, String> payload, HttpSession session) {
        String password = payload.get("password");

        if (reviewerPassword.equals(password)) {
            session.setAttribute("isReviewerAuthenticated", true);
            return ResponseEntity.ok(Map.of("success", true));
        }

        return ResponseEntity.status(401).body(Map.of("success", false, "message", "密码错误"));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> payload, HttpSession session) {
        String password = payload.get("password");

        System.out.println("Admin login attempt - Received password: " + (password != null ? "***" : "NULL"));
        System.out.println("Admin login attempt - Expected password: " + (adminPassword != null ? "***" : "NULL"));

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "密码不能为空"));
        }

        if (adminPassword.equals(password)) {
            session.setAttribute("isAdminAuthenticated", true);
            System.out.println("Admin login successful");
            return ResponseEntity.ok(Map.of("success", true));
        }

        System.out.println("Admin login failed - password mismatch");
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "密码错误"));
    }

    @GetMapping("/reviewer/check")
    public ResponseEntity<?> checkReviewerAuth(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isReviewerAuthenticated");
        return ResponseEntity.ok(Map.of("authenticated", isAuthenticated != null && isAuthenticated));
    }

    @GetMapping("/admin/check")
    public ResponseEntity<?> checkAdminAuth(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAdminAuthenticated");
        return ResponseEntity.ok(Map.of("authenticated", isAuthenticated != null && isAuthenticated));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true));
    }

}