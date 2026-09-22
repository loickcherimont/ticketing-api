package com.github.loickcherimont.ticketing_api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.loickcherimont.ticketing_api.models.Role;
import com.github.loickcherimont.ticketing_api.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketRequestDto;
import com.github.loickcherimont.ticketing_api.exceptions.TicketExistingTitleException;
import com.github.loickcherimont.ticketing_api.exceptions.TicketNotFoundException;
import com.github.loickcherimont.ticketing_api.models.Ticket;
import com.github.loickcherimont.ticketing_api.models.TicketStatus;
import com.github.loickcherimont.ticketing_api.repository.TicketRepository;
import com.github.loickcherimont.ticketing_api.services.impl.TicketServiceImpl;

/**
 * Unit tests for {@link TicketServiceImpl}.
 *
 * <p>
 * All dependencies (repository) are mocked with Mockito:
 * no database is accessed during test execution.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    // -------------------------------------------------------------------------
    // Shared test data
    // -------------------------------------------------------------------------

    private static final UUID TICKET_ID = UUID.randomUUID();
    private static final String TICKET_TITLE = "Virement bancaire non reçu";
    private static final String TICKET_DESC = "Le client indique qu'un virement SEPA effectué il y a 72 heures "
            + "n'apparaît toujours pas sur son compte courant.";
    private static final String TICKET_SOLUTION = "Le virement SEPA a été localisé en cours de traitement. "
            + "Un délai supplémentaire de 24 à 48 heures est nécessaire en raison "
            + "d'un contrôle de conformité. Le client sera notifié dès que les fonds "
            + "seront crédités sur son compte courant.";
    private static final User AGENT_USER = new User(
            UUID.randomUUID(), "agent@company.com", "password", Role.AGENT);
    private static final User ROLE_USER = new User(
            UUID.randomUUID(), "user@company.com", "password", Role.USER);
    private static final User TICKET_CREATED_BY = ROLE_USER;
    private static final User TICKET_ASSIGNED_TO = new User(
            UUID.randomUUID(), "other.agent@company.com", "password", Role.AGENT);

    // -------------------------------------------------------------------------
    // Mocks and class under test
    // -------------------------------------------------------------------------

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    // -------------------------------------------------------------------------
    // Tests — getAllTickets scoping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllTickets: should return all tickets for AGENT")
    void shouldReturnAllTicketsForAgent() {

        List<Ticket> tickets = List.of(
                new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.OPEN, null, TICKET_CREATED_BY, null));

        when(ticketRepository.findAll()).thenReturn(tickets);

        List<Ticket> result = ticketService.getAllTickets(AGENT_USER);

        assertThat(result).containsExactlyElementsOf(tickets);
        verify(ticketRepository).findAll();
    }

    @Test
    @DisplayName("getAllTickets: should return only the current USER tickets")
    void shouldReturnOnlyTicketsCreatedByCurrentUser() {

        List<Ticket> tickets = List.of(
                new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.OPEN, null, ROLE_USER, null));

        when(ticketRepository.findAllByCreatedBy(ROLE_USER)).thenReturn(tickets);

        List<Ticket> result = ticketService.getAllTickets(ROLE_USER);

        assertThat(result).containsExactlyElementsOf(tickets);
        verify(ticketRepository).findAllByCreatedBy(ROLE_USER);
    }

    // -------------------------------------------------------------------------
    // Tests — getTicketByIdAndCreatedBy
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getTicketByIdAndCreatedBy: should return ticket for AGENT when identifier exists")
    void shouldReturnTicketIfIdExistsForAgent() {

        Ticket ticket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.IN_PROGRESS,
                null, TICKET_CREATED_BY, TICKET_ASSIGNED_TO);

        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketByIdAndCreatedBy(TICKET_ID, AGENT_USER);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TICKET_ID);
        verify(ticketRepository).findById(TICKET_ID);
    }

    @Test
    @DisplayName("getTicketByIdAndCreatedBy: should return ticket for USER when he is the owner")
    void shouldReturnTicketIfCurrentUserIsOwner() {

        Ticket ticket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.IN_PROGRESS,
                null, ROLE_USER, TICKET_ASSIGNED_TO);

        when(ticketRepository.findByIdAndCreatedBy(TICKET_ID, ROLE_USER)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketByIdAndCreatedBy(TICKET_ID, ROLE_USER);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TICKET_ID);
        verify(ticketRepository).findByIdAndCreatedBy(TICKET_ID, ROLE_USER);
    }

    @Test
    @DisplayName("getTicketByIdAndCreatedBy: should throw TicketNotFoundException for USER not owning the ticket")
    void shouldThrowTicketNotFoundExceptionWhenCurrentUserDoesNotOwnTicket() {

        UUID NOT_OWNED_TICKET_ID = UUID.randomUUID();
        String notFoundMessage = String.format("Ticket %s introuvable", NOT_OWNED_TICKET_ID);

        when(ticketRepository.findByIdAndCreatedBy(NOT_OWNED_TICKET_ID, ROLE_USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketByIdAndCreatedBy(NOT_OWNED_TICKET_ID, ROLE_USER))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessage(notFoundMessage);

        verify(ticketRepository).findByIdAndCreatedBy(NOT_OWNED_TICKET_ID, ROLE_USER);
    }

    // -------------------------------------------------------------------------
    // Tests — createTicket
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createTicket: should return created ticket with OPEN status, no solution and current user as creator")
    void shouldReturnNewCreatedTicket() {

        TicketRequestDto request = new TicketRequestDto(TICKET_TITLE, TICKET_DESC);

        Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.OPEN,
                null, ROLE_USER, null);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        Ticket result = ticketService.createTicket(request, ROLE_USER);

        assertThat(result.getId()).isEqualTo(TICKET_ID);
        assertThat(result.getTitle()).isEqualTo(TICKET_TITLE);
        assertThat(result.getDescription()).isEqualTo(TICKET_DESC);
        assertThat(result.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(result.getSolution()).isNull();
        assertThat(result.getCreatedBy()).isEqualTo(ROLE_USER);

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());
        assertThat(ticketCaptor.getValue().getCreatedBy()).isEqualTo(ROLE_USER);
    }

    @Test
    @DisplayName("createTicket: should throw TicketExistingTitleException when title is already used")
    void shouldThrowTicketExistingTitleExceptionForDuplicateTitle() {

        TicketRequestDto request = new TicketRequestDto(TICKET_TITLE, TICKET_DESC);

        when(ticketRepository.existsByTitle(request.title().trim())).thenReturn(true);

        assertThatThrownBy(() -> ticketService.createTicket(request, ROLE_USER))
                .isInstanceOf(TicketExistingTitleException.class)
                .hasMessage("Ce titre existe déjà, veuillez en choisir un autre.");

        verify(ticketRepository).existsByTitle(request.title().trim());
    }

    // -------------------------------------------------------------------------
    // Tests — solveTicket
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("solveTicket: should return ticket with CLOSED status and trimmed solution")
    void shouldReturnSolvedTicketWithClosedStatus() {

        Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.CLOSED,
                TICKET_SOLUTION, TICKET_CREATED_BY, TICKET_ASSIGNED_TO);

        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(savedTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        // The solution is intentionally provided with leading and trailing spaces
        // to verify that the service trims it before persisting.
        SolutionRequestDto solutionWithPadding = new SolutionRequestDto("  " + TICKET_SOLUTION + "        ");

        Ticket result = ticketService.solveTicket(TICKET_ID, solutionWithPadding);

        assertThat(result.getId()).isEqualTo(TICKET_ID);
        assertThat(result.getStatus()).isEqualTo(TicketStatus.CLOSED);
        assertThat(result.getSolution()).isEqualTo(TICKET_SOLUTION);
        verify(ticketRepository).findById(TICKET_ID);
        verify(ticketRepository).save(any(Ticket.class));
    }

    // -------------------------------------------------------------------------
    // Tests — setTicketInProgress
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setTicketInProgress: should return ticket with IN_PROGRESS status and no solution")
    void shouldReturnTicketWithInProgressStatus() {

        Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.IN_PROGRESS,
                null, TICKET_CREATED_BY, TICKET_ASSIGNED_TO);

        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(savedTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        Ticket result = ticketService.setTicketInProgress(TICKET_ID);

        assertThat(result.getId()).isEqualTo(TICKET_ID);
        assertThat(result.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(result.getSolution()).isNull();
        verify(ticketRepository).findById(TICKET_ID);
        verify(ticketRepository).save(any(Ticket.class));
    }

    // -------------------------------------------------------------------------
    // Tests — error scenarios (edge cases)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getTicketByIdAndCreatedBy: should throw TicketNotFoundException when identifier is unknown")
    void shouldThrowTicketNotFoundExceptionWhenIdDoesNotExist() {

        UUID UNKNOWN_TICKET_ID = UUID.randomUUID();
        String notFoundMessage = String.format("Ticket %s introuvable", UNKNOWN_TICKET_ID);

        when(ticketRepository.findById(UNKNOWN_TICKET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketByIdAndCreatedBy(UNKNOWN_TICKET_ID, AGENT_USER))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessage(notFoundMessage);

        verify(ticketRepository).findById(UNKNOWN_TICKET_ID);
    }
}