package dev.loickcherimont.ticketing_app.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.loickcherimont.ticketing_app.model.Ticket;
import dev.loickcherimont.ticketing_app.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PostMapping
public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
    Ticket created = ticketService.createTicket(ticket);
    return ResponseEntity
        .created(URI.create("/api/tickets/" + created.getId()))
        .body(created);
}


    // @PutMapping("/{id}")
    // public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @RequestBody Ticket ticket) {
    //     return ResponseEntity.ok(ticketService.updateTicket(id, ticket));
    // }

    @PatchMapping("/{id}/solve")
    public ResponseEntity<Ticket> solveTicket(@PathVariable Long id, @RequestBody SolveTicketRequest request) {
        return ResponseEntity.ok(ticketService.solveTicket(id, request.getSolution()));
        // return ResponseEntity.ok(ticketService.solveTicket(id, ticket.getSolution()));
    }

    @PatchMapping("/{id}/in-progress")
    public ResponseEntity<Ticket> setTicketInProgress(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.setTicketInProgress(id));
    }

    // Inner class for the solve ticket request

    // To avoid the access to other fields than solution.
    public static class SolveTicketRequest {
        private String solution;

        public String getSolution() {
            return solution;
        }

        public void setSolution(String solution) {
            this.solution = solution;
        }
    }
}
