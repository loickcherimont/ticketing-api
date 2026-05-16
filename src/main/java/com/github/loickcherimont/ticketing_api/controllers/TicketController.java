package com.github.loickcherimont.ticketing_api.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.loickcherimont.ticketing_api.services.TicketService;

import lombok.RequiredArgsConstructor;

import com.github.loickcherimont.ticketing_api.models.Ticket;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    /**
     * Retrieve a ticket by its ID.
     *
     * @param id Ticket identifier
     * @return the ticket if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    /**
     * Create a new ticket.
     *
     * <p>
     * The ticket is created from the request body and persisted in the database.
     * </p>
     *
     * @param ticket ticket data sent by the client
     * @return ResponseEntity containing the created ticket and
     *         {@code HTTP 201 CREATED status}
     */
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        Ticket created = ticketService.createTicket(ticket);
        return ResponseEntity
                .created(URI.create("/api/tickets/" + created.getId()))
                .body(created);
    }

    /**
     * Mark a ticket as solved and attach a solution.
     *
     * @param id      ticket identifier
     * @param request request body containing the solution text
     * @return updated ticket with status {@code SOLVED}
     */
    @PatchMapping("/{id}/solve")
    public ResponseEntity<Ticket> solveTicket(@PathVariable Long id, @RequestBody SolveTicketRequest request) {
        return ResponseEntity.ok(ticketService.solveTicket(id, request.getSolution()));
    }

    /**
     * Set a ticket status to {@code IN_PROGRESS}.
     *
     * @param id ticket identifier
     * @return updated ticket with {@code IN_PROGRESS} status
     */
    @PatchMapping("/{id}/in-progress")
    public ResponseEntity<Ticket> setTicketInProgress(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.setTicketInProgress(id));
    }

    /**
     * Inner class for the solve ticket request
     * To avoid the access to other fields than solution.
     */
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