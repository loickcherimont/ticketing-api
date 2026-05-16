package com.github.loickcherimont.ticketing_api.services.impl;

import java.util.List;
import org.springframework.stereotype.Service;

import com.github.loickcherimont.ticketing_api.exceptions.TicketNotFoundException;
import com.github.loickcherimont.ticketing_api.models.Ticket;
import com.github.loickcherimont.ticketing_api.models.TicketStatus;
import com.github.loickcherimont.ticketing_api.repository.TicketRepository;
import com.github.loickcherimont.ticketing_api.services.TicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Retrieve a ticket by its ID.
     *
     * @param id Ticket identifier
     * 
     * @return the ticket if found
     * @throws TicketNotFoundException if no ticket exists with the given id
     */
    @Override
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + id));
    }

    /**
     * Create and save ticket in database.
     *
     * @param ticket Ticket informations
     * 
     * @return The new created ticket
     */
    @Override
    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Solve a ticket by attaching a solution and saving the update in database.
     *
     * <p>
     * Once solved, the ticket status is automatically set to {@code CLOSED}.
     * </p>
     *
     * @param id       Ticket identifier
     * @param solution Solution attached to the ticket
     *
     * @return The updated ticket with the provided solution and {@code CLOSED}
     *         status
     */
    @Override
    public Ticket solveTicket(Long id, String solution) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.setSolution(solution);
        existingTicket.setStatus(TicketStatus.CLOSED);
        return ticketRepository.save(existingTicket);
    }

    /**
     * Change ticket status and solution to {@code IN_PROGRESS} and saving the update in database.
     *
     * @param id Ticket identifier
     *
     * @return The updated ticket with {@code IN_PROGRESS} status
     */
    @Override
    public Ticket setTicketInProgress(Long id) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.setStatus(TicketStatus.IN_PROGRESS);
        existingTicket.setSolution("Ticket #" + existingTicket.getId() + " in progress");
        return ticketRepository.save(existingTicket);
    }

}
