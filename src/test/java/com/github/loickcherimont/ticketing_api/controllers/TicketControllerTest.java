package com.github.loickcherimont.ticketing_api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.github.loickcherimont.ticketing_api.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
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
import com.github.loickcherimont.ticketing_api.filter.JwtAuthenticationFilter;
import com.github.loickcherimont.ticketing_api.models.Role;
import com.github.loickcherimont.ticketing_api.models.Ticket;
import com.github.loickcherimont.ticketing_api.models.TicketStatus;
import com.github.loickcherimont.ticketing_api.services.JwtService;
import com.github.loickcherimont.ticketing_api.services.TicketService;

import jakarta.servlet.FilterChain;

/**
 * Web MVC tests for {@link TicketController}.
 *
 * <p>
 * Security configuration is imported using
 * {@code @Import(SecurityConfig.class)} so that
 * role-based access rules are evaluated during the MVC slice.
 * </p>
 *
 * <p>
 * Authenticated requests use {@link SecurityMockMvcRequestPostProcessors#user}
 * so that the {@code principal} is the application {@link User} entity
 * (like the production JWT filter does), making {@code @AuthenticationPrincipal}
 * resolution realistic.
 * </p>
 */
@WebMvcTest(TicketController.class)
@Import(SecurityConfig.class)
public class TicketControllerTest {

	private static final UUID TICKET_ID = UUID.randomUUID();
	private static final String TICKET_TITLE = "Virement bancaire non reçu";
	private static final String TICKET_DESCRIPTION = "Le client indique qu’un virement SEPA effectué il y a 72 heures n’apparaît toujours pas sur son compte courant.";
	private static final String TICKET_SOLUTION = "Le virement SEPA a été localisé en cours de traitement. Un délai supplémentaire de 24 à 48 heures est nécessaire en raison d'un contrôle de conformité. Le client sera notifié dès que les fonds seront crédités sur son compte courant.";
	private static final User TICKET_CREATED_BY = new User(
			UUID.randomUUID(), "client@banque.fr", "password", Role.USER);
	private static final User TICKET_ASSIGNED_TO = new User(
			UUID.randomUUID(), "agent@banque.fr", "password", Role.AGENT);
	private static final String BASE_URI = "/api/tickets";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TicketService ticketService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@BeforeEach
	void allowJwtFilterChainToContinue() throws Exception {
		doAnswer(invocation -> {
			FilterChain filterChain = invocation.getArgument(2);
			filterChain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
			return null;
		}).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
	}

	// -------------------------------------------------------------------------
	// Tests — happy paths
	// -------------------------------------------------------------------------

	@Test
	@DisplayName("createTicket: should return HTTP 201 Created for authenticated AGENT or USER")
	void shouldReturnHttp201WithNewCreatedTicket() throws Exception {

		TicketRequestDto request = new TicketRequestDto(TICKET_TITLE, TICKET_DESCRIPTION);
		Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESCRIPTION, TicketStatus.OPEN, null,
				TICKET_CREATED_BY, null);

		when(ticketService.createTicket(any(TicketRequestDto.class), any(User.class))).thenReturn(savedTicket);

		mockMvc.perform(post(BASE_URI)
				.with(user(TICKET_CREATED_BY))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(TICKET_ID.toString()))
				.andExpect(jsonPath("$.title").value(TICKET_TITLE))
				.andExpect(jsonPath("$.description").value(TICKET_DESCRIPTION))
				.andExpect(jsonPath("$.status").value(TicketStatus.OPEN.name()))
				.andExpect(jsonPath("$.solution").isEmpty());

		verify(ticketService).createTicket(any(TicketRequestDto.class), any(User.class));
	}

	@Test
	@DisplayName("getAllTickets: should return HTTP 200 OK for authenticated AGENT")
	void shouldReturnHttp200AndAllTicketsForAuthenticatedAgent() throws Exception {

		List<Ticket> tickets = List.of(
				new Ticket(UUID.fromString("47d57b59-1859-4bba-ad68-b82240492306"),
						"Carte bancaire bloquée",
						"Le client signale que sa carte bancaire a été bloquée suite à 3 tentatives de code PIN erronées.",
						TicketStatus.OPEN,
						null, TICKET_CREATED_BY, null),
				new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESCRIPTION, TicketStatus.IN_PROGRESS,
						null, TICKET_CREATED_BY, TICKET_ASSIGNED_TO),
				new Ticket(UUID.fromString("bc17a8a6-5630-4c21-8384-53c79b300570"),
						"Prélèvement non autorisé",
						"Le client conteste un prélèvement de 149,99€ apparu sur son relevé de compte qu'il n'a pas autorisé.",
						TicketStatus.CLOSED,
						"Après vérification, le prélèvement a été identifié comme frauduleux et remboursé "
								+ "intégralement sous 48 heures. Une nouvelle carte bancaire a été émise et envoyée à "
								+ "l'adresse du client.", TICKET_CREATED_BY, TICKET_ASSIGNED_TO));

		when(ticketService.getAllTickets(TICKET_ASSIGNED_TO)).thenReturn(tickets);

		mockMvc.perform(get(BASE_URI).with(user(TICKET_ASSIGNED_TO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(tickets.size()))
				.andExpect(jsonPath("$[0].id").exists())
				.andExpect(jsonPath("$[0].title").exists())
				.andExpect(jsonPath("$[0].description").exists())
				.andExpect(jsonPath("$[0].status").exists())
				.andExpect(jsonPath("$[0].solution").isEmpty());

		verify(ticketService).getAllTickets(TICKET_ASSIGNED_TO);
	}

	@Test
	@DisplayName("getAllTickets: should return HTTP 200 OK for authenticated USER with his tickets only")
	void shouldReturnHttp200AndAllTicketsCreatedByAuthenticatedUser() throws Exception {

		List<Ticket> tickets = List.of(
				new Ticket(UUID.fromString("47d57b59-1859-4bba-ad68-b82240492306"),
						"Carte bancaire bloquée",
						"Le client signale que sa carte bancaire a été bloquée suite à 3 tentatives de code PIN erronées.",
						TicketStatus.OPEN,
						null, TICKET_CREATED_BY, null),
				new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESCRIPTION, TicketStatus.IN_PROGRESS,
						null, TICKET_CREATED_BY, TICKET_ASSIGNED_TO));

		when(ticketService.getAllTickets(TICKET_CREATED_BY)).thenReturn(tickets);

		mockMvc.perform(get(BASE_URI).with(user(TICKET_CREATED_BY)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(tickets.size()))
				.andExpect(jsonPath("$[0].id").exists())
				.andExpect(jsonPath("$[0].title").exists())
				.andExpect(jsonPath("$[0].description").exists())
				.andExpect(jsonPath("$[0].status").exists())
				.andExpect(jsonPath("$[0].solution").isEmpty());

		verify(ticketService).getAllTickets(TICKET_CREATED_BY);
	}

	@Test
	@DisplayName("getTicketByIdAndCreatedBy: should return HTTP 200 OK for authenticated AGENT or USER")
	void shouldReturnHttp200AndTicketIfIdExists() throws Exception {

		Ticket ticket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESCRIPTION, TicketStatus.IN_PROGRESS,
				null, TICKET_CREATED_BY, TICKET_ASSIGNED_TO);

		when(ticketService.getTicketByIdAndCreatedBy(TICKET_ID, TICKET_CREATED_BY)).thenReturn(ticket);

		mockMvc.perform(get(String.format(BASE_URI + "/%s", TICKET_ID)).with(user(TICKET_CREATED_BY)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TICKET_ID.toString()));

		verify(ticketService).getTicketByIdAndCreatedBy(TICKET_ID, TICKET_CREATED_BY);
	}

	@Test
	@DisplayName("solveTicket: should return HTTP 200 OK for authenticated AGENT")
	@WithMockUser(roles = { "AGENT" })
	void shouldReturnHttp200WithSolvedAndClosedTicket() throws Exception {

		Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESCRIPTION, TicketStatus.CLOSED,
				TICKET_SOLUTION, TICKET_CREATED_BY, TICKET_ASSIGNED_TO);
		SolutionRequestDto solutionRequest = new SolutionRequestDto("  " + TICKET_SOLUTION + "  ");

		when(ticketService.solveTicket(TICKET_ID, solutionRequest)).thenReturn(savedTicket);

		mockMvc.perform(patch(String.format(BASE_URI + "/%s/solve", TICKET_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(solutionRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TICKET_ID.toString()))
				.andExpect(jsonPath("$.status").value(TicketStatus.CLOSED.name()))
				.andExpect(jsonPath("$.solution").value(TICKET_SOLUTION));

		verify(ticketService).solveTicket(TICKET_ID, solutionRequest);
	}

	@Test
	@DisplayName("setTicketInProgress: should return HTTP 200 OK for authenticated AGENT")
	@WithMockUser(roles = { "AGENT" })
	void shouldReturnHttp200WithTicketInProgressStatus() throws Exception {

		Ticket savedTicket = new Ticket(TICKET_ID, TICKET_TITLE, TICKET_DESCRIPTION, TicketStatus.IN_PROGRESS,
				null, TICKET_CREATED_BY, TICKET_ASSIGNED_TO);

		when(ticketService.setTicketInProgress(TICKET_ID)).thenReturn(savedTicket);

		mockMvc.perform(patch(String.format(BASE_URI + "/%s/in-progress", TICKET_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TICKET_ID.toString()))
				.andExpect(jsonPath("$.status").value(TicketStatus.IN_PROGRESS.name()))
				.andExpect(jsonPath("$.solution").isEmpty());

		verify(ticketService).setTicketInProgress(TICKET_ID);
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
		@DisplayName("solveTicket: should return HTTP 401 Unauthorized for anonymous AGENT")
		void shouldReturnHttp401ForAnonymousAgentOnSolveTicketRoute() throws Exception {

			mockMvc.perform(
					patch("/api/tickets/" + TICKET_ID + "/solve")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("setTicketInProgress: should return HTTP 401 Unauthorized for anonymous AGENT")
		void shouldReturnHttp401ForAnonymousAgentOnSetTicketInProgressRoute() throws Exception {

			mockMvc.perform(
					patch("/api/tickets/" + TICKET_ID + "/in-progress"))
					.andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("Forbidden users")
	class ForbiddenUsersTests {

		@Test
		@DisplayName("solveTicket: should return HTTP 403 Forbidden for authenticated USER NOT AGENT")
		@WithMockUser(roles = { "USER" })
		void shouldReturnHttp403ForUsersNotAgentOnSolveTicketRoute() throws Exception {

			mockMvc.perform(
					patch("/api/tickets/" + TICKET_ID + "/solve")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("setTicketInProgress: should return HTTP 403 Forbidden for authenticated USER NOT AGENT")
		@WithMockUser(roles = { "USER" })
		void shouldReturnHttp403ForUsersNotAgentOnSetTicketInProgressRoute() throws Exception {

			mockMvc.perform(
					patch("/api/tickets/" + TICKET_ID + "/in-progress"))
					.andExpect(status().isForbidden());
		}

	}

	@Test
	@DisplayName("getTicketByIdAndCreatedBy: should return HTTP 404 Not Found for authenticated AGENT or USER")
	void shouldReturnHttp404IfIdNotExists() throws Exception {

		UUID UNKNOWN_TICKET_ID = UUID.randomUUID();

		when(ticketService.getTicketByIdAndCreatedBy(UNKNOWN_TICKET_ID, TICKET_CREATED_BY))
				.thenThrow(new TicketNotFoundException("Ticket not found:" + UNKNOWN_TICKET_ID));

		this.mockMvc.perform(get("/api/tickets/" + UNKNOWN_TICKET_ID).with(user(TICKET_CREATED_BY)))
				.andExpect(status().isNotFound());

		verify(ticketService).getTicketByIdAndCreatedBy(UNKNOWN_TICKET_ID, TICKET_CREATED_BY);
	}

	/**
	 * Verifies that creating a ticket with blank fields returns HTTP 400 Bad
	 * Request.
	 *
	 * @param title       blank title value (null, empty, whitespace)
	 * @param description blank description value (null, empty, whitespace)
	 */
	@ParameterizedTest(name = "[{index}] title=''{0}'' description=''{1}'' should return HTTP 400 Bad Request")
	@MethodSource("blankInputs")
	@DisplayName("createTicket: should return HTTP 400 Bad Request for authenticated AGENT or USER")
	@WithMockUser(roles = { "USER", "AGENT" })
	void shouldReturnHttp400IfNewTicketFieldsAreBlank(String title, String description) throws Exception {

		TicketRequestDto badRequestDto = new TicketRequestDto(title, description);

		this.mockMvc.perform(
				post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(badRequestDto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("createTicket: should return HTTP 400 Bad Request when title exceeds 100 characters")
	@WithMockUser(roles = { "USER", "AGENT" })
	void shouldReturnHttp400WhenTitleExceedsMaxLength() throws Exception {

		TicketRequestDto badRequestDto = new TicketRequestDto("T".repeat(101), TICKET_DESCRIPTION);

		this.mockMvc.perform(
				post("/api/tickets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(badRequestDto)))
				.andExpect(status().isBadRequest());
	}

	/**
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