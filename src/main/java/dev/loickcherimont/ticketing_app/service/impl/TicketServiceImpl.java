package dev.loickcherimont.ticketing_app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.loickcherimont.ticketing_app.exception.TicketNotFoundException;
import dev.loickcherimont.ticketing_app.model.Ticket;
import dev.loickcherimont.ticketing_app.model.TicketStatus;
import dev.loickcherimont.ticketing_app.repository.TicketRepository;
import dev.loickcherimont.ticketing_app.service.TicketService;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketById(Long id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        if (ticket.isPresent()) {
            return ticket.get();
        }
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + id));
    }

    @Override
    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    // @Override
    // public Ticket updateTicket(Long id, Ticket ticket) {
    //     Ticket existingTicket = getTicketById(id);
    //     existingTicket.setTitle(ticket.getTitle());
    //     existingTicket.setDescription(ticket.getDescription());
    //     existingTicket.setStatus(ticket.getStatus());
    //     existingTicket.setSolution(ticket.getSolution());
    //     return ticketRepository.save(existingTicket);
    // }

    @Override
    public Ticket solveTicket(Long id, String solution) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.setSolution(solution);
        existingTicket.setStatus(TicketStatus.CLOSED);
        return ticketRepository.save(existingTicket);
    }

    @Override
    public Ticket setTicketInProgress(Long id) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.setStatus(TicketStatus.IN_PROGRESS);
        return ticketRepository.save(existingTicket);
    }

}
