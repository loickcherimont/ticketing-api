package dev.loickcherimont.ticketing_app.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import dev.loickcherimont.ticketing_app.exception.TicketNotFoundException;
import dev.loickcherimont.ticketing_app.model.Ticket;
import dev.loickcherimont.ticketing_app.model.TicketStatus;
import dev.loickcherimont.ticketing_app.repository.TicketRepository;
import dev.loickcherimont.ticketing_app.services.impl.TicketServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void shouldReturnTicketIfIdExists() throws Exception {

        Ticket ticket = new Ticket(
                2L,
                "Virement bancaire non reçu",
                "Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.",
                TicketStatus.IN_PROGRESS,
                null);

        when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket));

        Ticket actualTicket = ticketService.getTicketById(2L);
        assertThat(actualTicket).isNotNull();
        assertThat(actualTicket.getId()).isEqualTo(2L);
    }

    @Test
    void shouldReturnTicketNotFoundExceptionIfIdNotExists() throws Exception {

        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        /**
         * To verify if exception is thrown
         */
        assertThatThrownBy(() -> ticketService.getTicketById(99L))
            .isInstanceOf(TicketNotFoundException.class)
            .hasMessage("Ticket not found: 99");

    }
}