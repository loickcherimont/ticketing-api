package com.github.loickcherimont.ticketing_api.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.loickcherimont.ticketing_api.services.TicketService;

import lombok.RequiredArgsConstructor;

import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketRequestDto;
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
     * @param ticketRequestDto ticket data sent by the client
     * @return ResponseEntity containing the created ticket and
     *         {@code HTTP 201 CREATED status}
     */
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody TicketRequestDto ticketRequestDto) {
        Ticket created = ticketService.createTicket(ticketRequestDto);
        return ResponseEntity
                .created(URI.create("/api/tickets/" + created.getId()))
                .body(created);
    }

    /**
     * Mark a ticket as solved and attach a solution.
     *
     * @param id      ticket identifier
     * @param solutionRequestDto request body containing the solution text
     * @return updated ticket with status {@code SOLVED}
     */
    @PatchMapping("/{id}/solve")
    public ResponseEntity<Ticket> solveTicket(@PathVariable Long id, @RequestBody SolutionRequestDto solutionRequestDto) {
        return ResponseEntity.ok(ticketService.solveTicket(id, solutionRequestDto));
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
}