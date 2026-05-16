package com.github.loickcherimont.ticketing_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.loickcherimont.ticketing_api.models.Ticket;


@Repository 
public interface TicketRepository extends JpaRepository <Ticket, Long> {}
