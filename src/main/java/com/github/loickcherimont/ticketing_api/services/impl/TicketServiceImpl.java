package com.github.loickcherimont.ticketing_api.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.github.loickcherimont.ticketing_api.models.Role;
import com.github.loickcherimont.ticketing_api.models.User;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public List<Ticket> getAllTickets(User currentUser) {

        if (Objects.requireNonNull(currentUser.getRole()) == Role.AGENT) {
            return ticketRepository.findAll();
        }
        return ticketRepository.findAllByCreatedBy(currentUser);
    }

    /**
     * Retrieve a ticket by its ID for AGENT.
     * Retrieve user ticket using ticket id and the current user.
     *
     * @param id   Ticket identifier
     * @param user Ticket owner
     * @return the ticket if found
     * @throws TicketNotFoundException if no ticket exists with the given id
     */
    @Override
    public Ticket getTicketByIdAndCreatedBy(UUID id, User currentUser) {

        if (Objects.requireNonNull(currentUser.getRole()) == Role.AGENT) {
            return findTicketById(id);
        }
        return ticketRepository.findByIdAndCreatedBy(id, currentUser).orElseThrow(() -> new TicketNotFoundException("Ticket " + id + " introuvable"));

    }

    /**
     * Create and save ticket in database.
     *
     * @param ticketRequestDto Ticket informations from client
     * @param currentUser
     * @return The new created ticket
     */
    @Override
    public Ticket createTicket(TicketRequestDto ticketRequestDto, User currentUser) {

        if (ticketRepository.existsByTitle(ticketRequestDto.title().trim())) {
            throw new TicketExistingTitleException("Ce titre existe déjà, veuillez en choisir un autre.");
        }

        Ticket newTicket = new Ticket();

        newTicket.setTitle(ticketRequestDto.title().trim());
        newTicket.setDescription(ticketRequestDto.description().trim());
        newTicket.setStatus(TicketStatus.OPEN);
        newTicket.setCreatedBy(currentUser);

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
    @PreAuthorize("hasRole('AGENT')")
    public Ticket solveTicket(UUID id, SolutionRequestDto solutionRequestDto) {
        Ticket existingTicket = findTicketById(id);
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
    @PreAuthorize("hasRole('AGENT')")
    public Ticket setTicketInProgress(UUID id) {
        Ticket existingTicket = findTicketById(id);
        existingTicket.setStatus(TicketStatus.IN_PROGRESS);
        return ticketRepository.save(existingTicket);
    }

    /**
     * Internal helper shared by all operations that need a ticket regardless of
     * ownership: only AGENT-guarded methods and the AGENT branch of the lookup
     * methods use it.
     *
     * @param id Ticket identifier
     * @return the ticket if found
     * @throws TicketNotFoundException if no ticket exists with the given id
     */
    private Ticket findTicketById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket " + id + " introuvable"));
    }

}
