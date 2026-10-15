package com.github.loickcherimont.ticketing_api.services;

import java.util.List;
import java.util.UUID;

import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketResponseDto;
import com.github.loickcherimont.ticketing_api.models.User;

public interface TicketService {
    List<TicketResponseDto> getAllTickets(User user);
    TicketResponseDto getTicketByIdAndCreatedBy(UUID id, User currentUser);
    TicketResponseDto createTicket(TicketRequestDto ticketRequestDto, User currentUser);
    TicketResponseDto solveTicket(UUID id, SolutionRequestDto solutionRequestDto);
    TicketResponseDto setTicketInProgress(UUID id);
}
