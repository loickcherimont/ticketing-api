package com.github.loickcherimont.ticketing_api.services;

import java.util.List;
import java.util.UUID;

import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketRequestDto;
import com.github.loickcherimont.ticketing_api.models.Ticket;
import com.github.loickcherimont.ticketing_api.models.User;

public interface TicketService {
    List<Ticket> getAllTickets(User user);
    Ticket getTicketByIdAndCreatedBy(UUID id, User currentUser);
    Ticket createTicket(TicketRequestDto ticketRequestDto, User currentUser);
    Ticket solveTicket(UUID id, SolutionRequestDto solutionRequestDto);
    Ticket setTicketInProgress(UUID id);
}
