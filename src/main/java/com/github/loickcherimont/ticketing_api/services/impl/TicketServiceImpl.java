package com.github.loickcherimont.ticketing_api.services.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketRequestDto;
import com.github.loickcherimont.ticketing_api.exceptions.TicketExistingTitleException;
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
        return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException("Ticket " + id + " introuvable"));
    }

    /**
     * Create and save ticket in database.
     *
     * @param ticketRequestDto Ticket informations from client
     * 
     * @return The new created ticket
     */
    @Override
    public Ticket createTicket(TicketRequestDto ticketRequestDto) {

        if (ticketRepository.existsByTitle(ticketRequestDto.title().trim())) {
            throw new TicketExistingTitleException("Ce titre existe déjà, veuillez en choisir un autre.");
        }

        Ticket newTicket = new Ticket();

        newTicket.setTitle(ticketRequestDto.title().trim());
        newTicket.setDescription(ticketRequestDto.description().trim());
        newTicket.setStatus(TicketStatus.OPEN);

        return ticketRepository.save(newTicket);
    }

    /**
     * Solve a ticket by attaching a solution and saving the update in database.
     *
     * <p>
     * Once solved, the ticket status is automatically set to {@code CLOSED}.
     * </p>
     *
     * @param id                 Ticket identifier
     * @param solutionRequestDto Object containing the attached solution to the
     *                           ticket
     *
     * @return The updated ticket with the provided solution and {@code CLOSED}
     *         status
     */
    @Override
    public Ticket solveTicket(Long id, SolutionRequestDto solutionRequestDto) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.setSolution(solutionRequestDto.solution().trim());
        existingTicket.setStatus(TicketStatus.CLOSED);
        return ticketRepository.save(existingTicket);
    }

    /**
     * Change ticket status and solution to {@code IN_PROGRESS} and saving the
     * update in database.
     *
     * @param id Ticket identifier
     *
     * @return The updated ticket with {@code IN_PROGRESS} status
     */
    @Override
    public Ticket setTicketInProgress(Long id) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.setStatus(TicketStatus.IN_PROGRESS);
        existingTicket.setSolution("Ticket " + existingTicket.getId() + " en cours");
        return ticketRepository.save(existingTicket);
    }

}
