package com.github.loickcherimont.ticketing_api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.loickcherimont.ticketing_api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.loickcherimont.ticketing_api.models.Ticket;


@Repository 
public interface TicketRepository extends JpaRepository <Ticket, UUID> {
    boolean existsByTitle(String title);
    List<Ticket> findAllByCreatedBy(User user);
    Optional<Ticket> findByIdAndCreatedBy(UUID id, User user);
}
