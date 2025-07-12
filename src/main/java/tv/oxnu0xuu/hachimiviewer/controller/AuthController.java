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

    @Autowired
    @Qualifier("reviewerPassword")
    private String reviewerPassword;

    @Autowired
    @Qualifier("adminPassword")
    private String adminPassword;

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

        if (adminPassword.equals(password)) {
            session.setAttribute("isAdminAuthenticated", true);
            return ResponseEntity.ok(Map.of("success", true));
        }

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