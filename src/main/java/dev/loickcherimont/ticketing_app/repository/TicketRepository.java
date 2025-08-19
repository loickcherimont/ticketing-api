package dev.loickcherimont.ticketing_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.loickcherimont.ticketing_app.model.Ticket;


@Repository // Optional annotation, but it's good practice to keep it.
public interface TicketRepository extends JpaRepository <Ticket, Long> {

}
