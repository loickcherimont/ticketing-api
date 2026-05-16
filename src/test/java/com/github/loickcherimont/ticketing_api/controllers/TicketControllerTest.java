package com.github.loickcherimont.ticketing_api.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.loickcherimont.ticketing_api.exceptions.TicketNotFoundException;
import com.github.loickcherimont.ticketing_api.models.Ticket;
import com.github.loickcherimont.ticketing_api.models.TicketStatus;
import com.github.loickcherimont.ticketing_api.services.TicketService;

@WebMvcTest(TicketController.class)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

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
    }

    @Test
    void shouldReturnError404IfIdNotExists() throws Exception {

        when(ticketService.getTicketById(99L)).thenThrow(new TicketNotFoundException("Ticket not found: 99"));

        this.mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound());

    }
}
