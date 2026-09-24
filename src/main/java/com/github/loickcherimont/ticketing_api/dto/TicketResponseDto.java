package com.github.loickcherimont.ticketing_api.dto;

import java.util.UUID;

import com.github.loickcherimont.ticketing_api.models.Ticket;
import com.github.loickcherimont.ticketing_api.models.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket response payload, creator and assignee exposed as emails")
public record TicketResponseDto(
        @Schema(description = "Ticket unique identifier") UUID id,
        @Schema(description = "Ticket title") String title,
        @Schema(description = "Ticket description") String description,
        @Schema(description = "Ticket status") TicketStatus status,
        @Schema(description = "Agent solution, null while the ticket is not solved") String solution,
        @Schema(description = "Email of the ticket creator") String createdByEmail,
        @Schema(description = "Email of the assigned agent, null if not assigned") String assignedToEmail
) {
    public static TicketResponseDto from(Ticket ticket) {
        return new TicketResponseDto(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getSolution(),
                ticket.getCreatedBy().getEmail(),
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getEmail() : null
        );
    }
}