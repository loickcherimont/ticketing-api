package dev.loickcherimont.ticketing_app.services;

import java.util.List;

import dev.loickcherimont.ticketing_app.model.Ticket;

public interface TicketService {
    List<Ticket> getAllTickets();
    Ticket getTicketById(Long id);
    Ticket createTicket(Ticket ticket);
    // Ticket updateTicket(Long id, Ticket ticket);
    Ticket solveTicket(Long id, String solution);
    Ticket setTicketInProgress(Long id);
}
