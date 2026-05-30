package com.github.loickcherimont.ticketing_api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loickcherimont.ticketing_api.configuration.SecurityConfig;
import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketRequestDto;
import com.github.loickcherimont.ticketing_api.exceptions.TicketNotFoundException;
import com.github.loickcherimont.ticketing_api.models.Ticket;
import com.github.loickcherimont.ticketing_api.models.TicketStatus;
import com.github.loickcherimont.ticketing_api.services.JwtService;
import com.github.loickcherimont.ticketing_api.services.TicketService;

@WebMvcTest(TicketController.class)
@Import(SecurityConfig.class)
public class TicketControllerTest {

	@Autowired
	private MockMvc mockMvc;

	/**
	 * To serialize and simulate request body :
	 * To convert a POJO in JSON object
	 */
	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TicketService ticketService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	// -------------------------------------------------------------------------
	// Tests — happy paths
	// -------------------------------------------------------------------------

	@Test
	@DisplayName("createTicket: should return HTTP 201 Created for authenticated AGENT or USER")
	@WithMockUser(roles = { "USER", "AGENT" })
	void shouldReturnHttp201WithNewCreatedTicket() throws Exception {

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

		when(ticketService.createTicket(any(TicketRequestDto.class))).thenReturn(savedTicket);

		this.mockMvc.perform(
				post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(ticket)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(2L))
				.andExpect(jsonPath("$.title").value("Virement bancaire non reçu"))
				.andExpect(jsonPath("$.description").value(
						"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant."))
				.andExpect(jsonPath("$.status").value(TicketStatus.OPEN.name()))
				.andExpect(jsonPath("$.solution").isEmpty());

		verify(ticketService).createTicket(any(TicketRequestDto.class));

	}

	@Test
	@DisplayName("getAllTickets: should return HTTP 200 OK for authenticated AGENT or USER")
	@WithMockUser(roles = { "USER", "AGENT" })
	void shouldReturnHttp200AndAllTicketsForAuthenticatedAgentOrUser() throws Exception {

		List<Ticket> tickets = List.of(
				new Ticket(
						1L,
						"Carte bancaire bloquée",
						"Le client signale que sa carte bancaire a été bloquée suite à 3 tentatives de code PIN erronées.",
						TicketStatus.OPEN,
						null),

				new Ticket(
						2L,
						"Virement bancaire non reçu",
						"Le client indique qu'un virement SEPA effectué il y a 72 heures n'apparaît toujours pas sur son compte courant.",
						TicketStatus.IN_PROGRESS,
						null),
				new Ticket(
						3L,
						"Prélèvement non autorisé",
						"Le client conteste un prélèvement de 149,99€ apparu sur son relevé de compte qu'il n'a pas autorisé.",
						TicketStatus.CLOSED,
						"Après vérification, le prélèvement a été identifié comme frauduleux et remboursé intégralement sous 48 heures. Une nouvelle carte bancaire a été émise et envoyée à l'adresse du client."));

		when(ticketService.getAllTickets()).thenReturn(tickets);

		this.mockMvc.perform(get("/api/tickets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(3))

				/**
				 * We test that the list contains ticket, by checking only on 1 object ($[0])
				 * And we deduce with Java language properties
				 * The list contains only tickets of that form
				 */
				.andExpect(jsonPath("$[0].id").exists())
				.andExpect(jsonPath("$[0].title").exists())
				.andExpect(jsonPath("$[0].description").exists())
				.andExpect(jsonPath("$[0].status").exists())
				.andExpect(jsonPath("$[0].solution").isEmpty());

		verify(ticketService).getAllTickets();
	}

	@Test
	void shouldReturnTicketIfIdExists() throws Exception {

		Ticket ticket = new Ticket(
				2L,
				"Virement bancaire non reçu",
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.",
				TicketStatus.IN_PROGRESS,
				null);

		when(ticketService.getTicketById(2L)).thenReturn(ticket);

		this.mockMvc.perform(get("/api/tickets/2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2L));

		verify(ticketService).getTicketById(2L);
	}

	@Test
	void shouldReturnHttp200WithSolvedAndClosedTicket() throws Exception {

		/**
		 * Simulated ticket found in fake database
		 */
		Ticket savedTicket = new Ticket(
				2L,
				"Virement bancaire non reçu",
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.",
				TicketStatus.CLOSED,
				"Le virement SEPA a été localisé en cours de traitement. Un délai supplémentaire de 24 à 48 heures est nécessaire en raison d'un contrôle de conformité. Le client sera notifié dès que les fonds seront crédités sur son compte courant.");

		SolutionRequestDto solutionRequestDto = new SolutionRequestDto(
				"             Le virement SEPA a été localisé en cours de traitement. Un délai supplémentaire de 24 à 48 heures est nécessaire en raison d'un contrôle de conformité. Le client sera notifié dès que les fonds seront crédités sur son compte courant.     ");

		when(ticketService.solveTicket(2L, solutionRequestDto)).thenReturn(savedTicket);

		this.mockMvc.perform(
				patch("/api/tickets/2/solve")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(solutionRequestDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2L))
				.andExpect(jsonPath("$.status").value(TicketStatus.CLOSED.name()))
				.andExpect(jsonPath("$.solution").value(
						"Le virement SEPA a été localisé en cours de traitement. Un délai supplémentaire de 24 à 48 heures est nécessaire en raison d'un contrôle de conformité. Le client sera notifié dès que les fonds seront crédités sur son compte courant."));

		verify(ticketService).solveTicket(2L, solutionRequestDto);

	}

	@Test
	void shouldReturnHttp200WithTicketInProgressStatus() throws Exception {

		/**
		 * Simulated ticket found in fake database
		 */
		Ticket savedTicket = new Ticket(
				2L,
				"Virement bancaire non reçu",
				"Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.",
				TicketStatus.IN_PROGRESS,
				"Ticket #2 in progress");

		when(ticketService.setTicketInProgress(2L)).thenReturn(savedTicket);

		this.mockMvc.perform(
				patch("/api/tickets/2/in-progress"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2L))
				.andExpect(jsonPath("$.status").value(TicketStatus.IN_PROGRESS.name()))
				.andExpect(jsonPath("$.solution").value(
						"Ticket #2 in progress"));

		verify(ticketService).setTicketInProgress(2L);
	}

	// -------------------------------------------------------------------------
	// Tests — error scenarios (edge cases)
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Unauthenticated users")
	class UnauthenticatedUsersTests {

		@Test
		@DisplayName("getAllTickets: should return HTTP 401 Unauthorized for anonymous AGENT or USER")
		void shouldReturnHttp401ForAnonymousUsersOnGetAllTicketsRoute() throws Exception {

			mockMvc.perform(get("/api/tickets"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("createTicket: should return HTTP 401 Unauthorized for anonymous AGENT or USER")
		void shouldReturnHttp401ForAnonymousUsersOnCreateTicketRoute() throws Exception {

			mockMvc.perform(
					post("/api/tickets")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("solveTicket: should return HTTP 403 Forbidden for anonymous AGENT")
		void shouldReturnHttp401ForAnonymousAgentOnSolveTicketRoute() throws Exception {

			mockMvc.perform(
					patch("/api/tickets/agent/2/solve")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("setTicketInProgress: should return HTTP 403 Forbidden for anonymous agent")
		void shouldReturnHttp401ForAnonymousAgentOnSetTicketInProgressRoute() throws Exception {

			mockMvc.perform(
					patch("/api/tickets/agent/2/in-progress"))
					.andExpect(status().isUnauthorized());
		}
	}

	@Test
	void shouldReturnError404IfIdNotExists() throws Exception {

		when(ticketService.getTicketById(99L)).thenThrow(new TicketNotFoundException("Ticket not found: 99"));

		this.mockMvc.perform(get("/api/tickets/99"))
				.andExpect(status().isNotFound());

		verify(ticketService).getTicketById(99L);

	}

	/**
	 * Verifies that creating a ticket with blank fields returns HTTP 400 Bad
	 * Request.
	 *
	 * @param title       blank title value (null, empty, whitespace)
	 * @param description blank description value (null, empty, whitespace)
	 * @
	 */
	@ParameterizedTest(name = "[{index}] title=''{0}'' description=''{1}'' should return HTTP 400 Bad Request")
	@MethodSource("blankInputs")
	void shouldReturnBadRequest400IfNewTicketFieldsAreBlank(String title, String description) throws Exception {

		TicketRequestDto badRequestDto = new TicketRequestDto(title, description);

		this.mockMvc.perform(
				post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(badRequestDto)))
				.andExpect(status().isBadRequest());
	}

	/**
	 * 
	 * Method containing in Stream all blank cases.
	 * 
	 * @return Stream of null, "", "valide", " ".
	 */
	static Stream<Arguments> blankInputs() {

		return Stream.of(
				Arguments.of(null, null),
				Arguments.of(null, ""),
				Arguments.of(null, " "),
				Arguments.of(null, "Description valide"),
				Arguments.of("", null),
				Arguments.of("", ""),
				Arguments.of("", " "),
				Arguments.of("", "Description valide"),
				Arguments.of(" ", null),
				Arguments.of(" ", ""),
				Arguments.of(" ", " "),
				Arguments.of(" ", "Description valide"),
				Arguments.of("Titre valide", null),
				Arguments.of("Titre valide", ""),
				Arguments.of("Titre valide", " "));

	}
}
