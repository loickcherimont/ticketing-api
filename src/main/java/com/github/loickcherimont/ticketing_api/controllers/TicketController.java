package com.github.loickcherimont.ticketing_api.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.loickcherimont.ticketing_api.services.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketRequestDto;
import com.github.loickcherimont.ticketing_api.models.Ticket;

/**
 * REST controller for helpdesk ticket management endpoints.
 *
 * <p>
 * <strong>Responsibility:</strong> Handles HTTP requests for ticket lifecycle management.
 * Supports creating new tickets, retrieving tickets, and managing ticket status transitions.
 * Enforces role-based access control: customers can create and view tickets,
 * support agents can additionally claim tickets and provide solutions.
 * </p>
 *
 * @see com.github.loickcherimont.ticketing_api.services.TicketService
 * @see com.github.loickcherimont.ticketing_api.models.Ticket
 * @see com.github.loickcherimont.ticketing_api.filter.JwtAuthenticationFilter
 */
@Tag(name = "Tickets", description = "Helpdesk management API for tickets")
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Create a new ticket.
     *
     * <p>
     * The ticket is created from the valid request body and persisted in the
     * database.
     * </p>
     *
     * @param ticketRequestDto ticket data sent by the client
     * @return ResponseEntity containing the created ticket and
     *         {@code HTTP 201 Created}
     * @throws Exception ResponseEntity containing the fields with errors and
     *                   {@code HTTP 400 Bad Request}
     */
    @Operation(summary = "Create a ticket", description = "Create a new ticket using `ticketRequestDto` fields and save it to database (requires authentication)")
    @ApiResponse(responseCode = "201", description = "Ticket created with success")
    @ApiResponse(responseCode = "400", description = "Blank or invalid fields")
    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    @ApiResponse(responseCode = "409", description = "Ticket with same title already exists")
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody TicketRequestDto ticketRequestDto) {
        Ticket created = ticketService.createTicket(ticketRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/api/tickets/" + created.getId()))
                .body(created);
    }

    @Operation(summary = "Get all tickets", description = "Fetch all tickets from database (requires authentication)")
    @ApiResponse(responseCode = "200", description = "Tickets retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.status(HttpStatus.OK).body(ticketService.getAllTickets());
    }

    /**
     * Retrieve a ticket by its ID.
     *
     * @param id Ticket identifier
     * @return the ticket if found
     */
    @Operation(summary = "Get ticket by id", description = "Get a specific ticket by its `id` (requires authentication)")
    @ApiResponse(responseCode = "200", description = "Ticket retrieved by `id`")
    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    @ApiResponse(responseCode = "404", description = "Ticket with specified `id` not found")
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@Parameter(description = "`id` of ticket") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ticketService.getTicketById(id));
    }

    /**
     * Mark a ticket as solved and attach a solution.
     *
     * @param id                 ticket identifier
     * @param solutionRequestDto request body containing the solution text
     * @return updated ticket with status {@code SOLVED}
     */
    @Operation(summary = "Solve ticket by id", description = "Add a solution for target ticket (AGENT role required)")
    @ApiResponse(responseCode = "200", description = "Ticket solved")
    @ApiResponse(responseCode = "400", description = "Ticket with invalid solution")
    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient role (AGENT role required)")
    @ApiResponse(responseCode = "404", description = "Ticket with specified `id` not found")
    @PatchMapping("/agent/{id}/solve")
    public ResponseEntity<Ticket> solveTicket(@PathVariable Long id,
            @Valid @RequestBody SolutionRequestDto solutionRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(ticketService.solveTicket(id, solutionRequestDto));
    }

    /**
     * Set a ticket status to {@code IN_PROGRESS}.
     *
     * @param id ticket identifier
     * @return updated ticket with {@code IN_PROGRESS} status
     */
    @Operation(summary = "Set ticket in progress", description = "Change ticket status to IN_PROGRESS (AGENT role required)")
    @ApiResponse(responseCode = "200", description = "Ticket marked as in progress")
    @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient role (AGENT role required)")
    @ApiResponse(responseCode = "404", description = "Ticket with specified `id` not found")
    @PatchMapping("/agent/{id}/in-progress")
    public ResponseEntity<Ticket> setTicketInProgress(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ticketService.setTicketInProgress(id));
    }
}