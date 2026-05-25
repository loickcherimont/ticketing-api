package com.github.loickcherimont.ticketing_api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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

		verify(ticketRepository).findById(2L);
	}

	@Test
	void shouldReturnNewCreatedTicket() throws Exception {

		/**
		 * Ticket informations from client
		 */
		TicketRequestDto ticket = new TicketRequestDto(
				"Virement bancaire non reçu",
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.");

		/**
		 * Simulated ticket found in fake database
		 */
		Ticket savedTicket = new Ticket(
				2L,
				"Virement bancaire non reçu",
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.",
				TicketStatus.OPEN,
				null);

		when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

		Ticket actualTicket = ticketService.createTicket(ticket);

		assertThat(actualTicket.getId()).isNotNull();
		assertThat(actualTicket.getId()).isEqualTo(2L);
		assertThat(actualTicket.getTitle()).isEqualTo("Virement bancaire non reçu");
		assertThat(actualTicket.getDescription()).isEqualTo(
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.");
		assertThat(actualTicket.getStatus()).isEqualTo(TicketStatus.OPEN);
		assertThat(actualTicket.getSolution()).isNull();

		verify(ticketRepository).save(any(Ticket.class));
	}

	@Test
	void shouldReturnSolvedTicketWithClosedStatus() throws Exception {

		Ticket savedTicket = new Ticket(
				2L,
				"Virement bancaire non reçu",
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.",
				TicketStatus.CLOSED,
				"Le virement SEPA a été localisé en cours de traitement. Un délai supplémentaire de 24 à 48 heures est nécessaire en raison d'un contrôle de conformité. Le client sera notifié dès que les fonds seront crédités sur son compte courant.");

		when(ticketRepository.findById(2L)).thenReturn(Optional.of(savedTicket));
		when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

		Ticket solvedTicket = ticketService.solveTicket(
				2L,
				new SolutionRequestDto(
						"  Le virement SEPA a été localisé en cours de traitement. Un délai supplémentaire de 24 à 48 heures est nécessaire en raison d'un contrôle de conformité. Le client sera notifié dès que les fonds seront crédités sur son compte courant.        "));

		assertThat(solvedTicket.getId()).isEqualTo(2L);
		assertThat(solvedTicket.getSolution()).isEqualTo(
				"Le virement SEPA a été localisé en cours de traitement. Un délai supplémentaire de 24 à 48 heures est nécessaire en raison d'un contrôle de conformité. Le client sera notifié dès que les fonds seront crédités sur son compte courant.");
		assertThat(solvedTicket.getStatus()).isEqualTo(TicketStatus.CLOSED);

		verify(ticketRepository).findById(2L);
		verify(ticketRepository).save(any(Ticket.class));
	}

	@Test
	void shouldReturnTicketWithInProgressStatus() throws Exception {

		/**
		 * Simulated ticket found in fake database
		 */
		Ticket savedTicket = new Ticket(
				2L,
				"Virement bancaire non reçu",
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.",
				TicketStatus.IN_PROGRESS,
				"Ticket #2 in progress");

		when(ticketRepository.findById(2L)).thenReturn(Optional.of(savedTicket));
		when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

		Ticket inProgressTicket = ticketService.setTicketInProgress(2L);

		assertThat(inProgressTicket.getId()).isEqualTo(2L);
		assertThat(inProgressTicket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
		assertThat(inProgressTicket.getSolution()).isEqualTo("Ticket #2 in progress");

		verify(ticketRepository).findById(2L);
		verify(ticketRepository).save(any(Ticket.class));
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

		verify(ticketRepository).findById(99L);

	}

	@Test
	void shouldReturnTicketExistingExceptionForDuplicateTitle() throws Exception {

		TicketRequestDto ticketRequestDto = new TicketRequestDto(
				"Virement bancaire non reçu",
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.");

		when(ticketRepository.existsByTitle(ticketRequestDto.title().trim())).thenReturn(true);

		assertThatThrownBy(() -> ticketService.createTicket(ticketRequestDto)).isInstanceOf(TicketExistingTitleException.class).hasMessage("This title exists, try another one.");

		verify(ticketRepository).existsByTitle(ticketRequestDto.title().trim());
	}
}