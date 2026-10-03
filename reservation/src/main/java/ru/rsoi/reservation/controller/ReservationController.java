package ru.rsoi.reservation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import ru.rsoi.reservation.dto.CreateReservationRequest;
import ru.rsoi.reservation.dto.ReturnRequest;
import ru.rsoi.reservation.entity.Reservation;
import ru.rsoi.reservation.repo.ReservationRepository;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationRepository repo;

    public ReservationController(ReservationRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Reservation> list(@AuthenticationPrincipal Jwt jwt) {
        String username = extractUsername(jwt);
        return repo.findByUsernameOrderByIdDesc(username);
    }

    @GetMapping("/count-active")
    public long countActive(@AuthenticationPrincipal Jwt jwt) {
        String username = extractUsername(jwt);
        return repo.countByUsernameAndStatus(username, "RENTED");
    }

    @PostMapping
    public Reservation create(@AuthenticationPrincipal Jwt jwt,
                              @RequestBody CreateReservationRequest req) {
        String username = extractUsername(jwt);
        Reservation r = new Reservation();
        r.setReservationUid(UUID.randomUUID());
        r.setUsername(username);
        r.setBookUid(req.bookUid());
        r.setLibraryUid(req.libraryUid());
        r.setStatus("RENTED");
        r.setStartDate(LocalDate.now());
        r.setTillDate(req.tillDate());
        return repo.save(r);
    }

    @PostMapping("/{reservationUid}/return")
    public ResponseEntity<Reservation> returnBook(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable UUID reservationUid,
                                                  @RequestBody ReturnRequest req) {
        String username = extractUsername(jwt);
        var rOpt = repo.findByReservationUid(reservationUid);
        if (rOpt.isEmpty()) return ResponseEntity.notFound().build();
        Reservation r = rOpt.get();
        if (!r.getUsername().equals(username)) return ResponseEntity.notFound().build();
        if (req.date() != null && req.date().isAfter(r.getTillDate())) {
            r.setStatus("EXPIRED");
        } else {
            r.setStatus("RETURNED");
        }
        return ResponseEntity.ok(repo.save(r));
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