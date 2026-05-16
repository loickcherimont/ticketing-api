package com.github.loickcherimont.ticketing_api.services;

import java.util.List;

import com.github.loickcherimont.ticketing_api.models.Ticket;

public interface TicketService {
    List<Ticket> getAllTickets();
    Ticket getTicketById(Long id);
    Ticket createTicket(Ticket ticket);
    Ticket solveTicket(Long id, String solution);
    Ticket setTicketInProgress(Long id);
}
