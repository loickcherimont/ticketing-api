package com.github.loickcherimont.ticketing_api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 *
 * <p>
 * Covered scenarios:
 * </p>
 * <ul>
 * <li>Retrieving an existing ticket by identifier</li>
 * <li>Creating a new ticket</li>
 * <li>Resolving a ticket (CLOSED status)</li>
 * <li>Setting a ticket to IN_PROGRESS status</li>
 * <li>Throwing {@link TicketNotFoundException} for an unknown id</li>
 * <li>Throwing {@link TicketExistingTitleException} for an already existing
 * title</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    // -------------------------------------------------------------------------
    // Shared test data
    // Centralized here: if a value changes, it only needs to be updated once.
    // -------------------------------------------------------------------------

    private static final UUID TICKET_ID = UUID.randomUUID();
    private static final String TICKET_TITLE = "Virement bancaire non reçu";
    private static final String TICKET_DESC = "Le client indique qu'un virement SEPA effectué il y a 72 heures "
            + "n'apparaît toujours pas sur son compte courant.";
    private static final String TICKET_SOLUTION = "Le virement SEPA a été localisé en cours de traitement. "
            + "Un délai supplémentaire de 24 à 48 heures est nécessaire en raison "
            + "d'un contrôle de conformité. Le client sera notifié dès que les fonds "
            + "seront crédités sur son compte courant.";

    // -------------------------------------------------------------------------
    // Mocks and class under test
    // -------------------------------------------------------------------------

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    // -------------------------------------------------------------------------
    // Tests — happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getTicketById: should return ticket when identifier exists")
    void shouldReturnTicketIfIdExists() {

        Ticket ticket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.IN_PROGRESS, null);

        when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketById(TICKET_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TICKET_ID);
        verify(ticketRepository).findById(TICKET_ID);
    }

    @Test
    @DisplayName("createTicket: should return created ticket with OPEN status and no solution")
    void shouldReturnNewCreatedTicket() {

        TicketRequestDto request = new TicketRequestDto(TICKET_TITLE, TICKET_DESC);

        Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.OPEN, null);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        Ticket result = ticketService.createTicket(request);

        assertThat(result.getId()).isEqualTo(TICKET_ID);
        assertThat(result.getTitle()).isEqualTo(TICKET_TITLE);
        assertThat(result.getDescription()).isEqualTo(TICKET_DESC);
        assertThat(result.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(result.getSolution()).isNull();
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("solveTicket: should return ticket with CLOSED status and trimmed solution")
    void shouldReturnSolvedTicketWithClosedStatus() {

        Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.CLOSED, TICKET_SOLUTION);

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

    @Test
    @DisplayName("setTicketInProgress: should return ticket with IN_PROGRESS status and no solution")
    void shouldReturnTicketWithInProgressStatus() {

        Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESC, TicketStatus.IN_PROGRESS,
                null);

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

    /**
     * Verifies that {@link TicketNotFoundException} is thrown when the requested
     * identifier does not exist in the database.
     *
     * <p>
     * The error message must contain the provided identifier so that
     * callers (controllers, logs) can quickly identify the missing ticket.
     * </p>
     */
    @Test
    @DisplayName("getTicketById: should throw TicketNotFoundException when identifier is unknown")
    void shouldThrowTicketNotFoundExceptionWhenIdDoesNotExist() {

        UUID UNKNOWN_TICKET_ID = UUID.randomUUID();
        String notFoundMessage = String.format("Ticket %s introuvable", UNKNOWN_TICKET_ID);

        when(ticketRepository.findById(UNKNOWN_TICKET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById(UNKNOWN_TICKET_ID))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessage(notFoundMessage);

        verify(ticketRepository).findById(UNKNOWN_TICKET_ID);
    }

    /**
     * Verifies that {@link TicketExistingTitleException} is thrown when a ticket
     * with the same title already exists.
     *
     * <p>
     * The title is trimmed in the service layer. This test ensures that
     * the repository validation is performed using the sanitized value.
     * </p>
     */
    @Test
    @DisplayName("createTicket: should throw TicketExistingTitleException when title is already used")
    void shouldThrowTicketExistingTitleExceptionForDuplicateTitle() {

        TicketRequestDto request = new TicketRequestDto(TICKET_TITLE, TICKET_DESC);

        when(ticketRepository.existsByTitle(request.title().trim())).thenReturn(true);

        assertThatThrownBy(() -> ticketService.createTicket(request))
                .isInstanceOf(TicketExistingTitleException.class)
                .hasMessage("Ce titre existe déjà, veuillez en choisir un autre.");

        verify(ticketRepository).existsByTitle(request.title().trim());
    }
}