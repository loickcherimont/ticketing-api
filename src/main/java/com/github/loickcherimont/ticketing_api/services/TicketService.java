package com.github.loickcherimont.ticketing_api.services;

import java.util.List;
import java.util.UUID;

import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketRequestDto;
import com.github.loickcherimont.ticketing_api.models.Ticket;

public interface TicketService {
    List<Ticket> getAllTickets();
    Ticket getTicketById(UUID id);
    Ticket createTicket(TicketRequestDto ticketRequestDto);
    Ticket solveTicket(UUID id, SolutionRequestDto solutionRequestDto);
    Ticket setTicketInProgress(UUID id);
}
