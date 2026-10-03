package ru.rsoi.rating.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import ru.rsoi.rating.service.RatingService;

@RestController
@RequestMapping("/api/v1/rating")
public class RatingController {

    private final RatingService service;

    public RatingController(RatingService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Integer> get(@AuthenticationPrincipal Jwt jwt) {
        String username = extractUsername(jwt);
        return Map.of("stars", service.getOrCreate(username).getStars());
    }

    @PostMapping
    public Map<String, Integer> change(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam("delta") int delta) {
        String username = extractUsername(jwt);
        return Map.of("stars", service.change(username, delta).getStars());
    }

    private String extractUsername(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException("No JWT principal");
        }
        String username = jwt.getClaimAsString("username");
        if (username != null && !username.isBlank()) {
            return username;
        }
        return jwt.getSubject();
    }
}