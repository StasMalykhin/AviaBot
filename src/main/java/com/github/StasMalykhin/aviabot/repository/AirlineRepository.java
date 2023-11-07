package com.github.StasMalykhin.aviabot.repository;

import com.github.StasMalykhin.aviabot.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Stanislav Malykhin
 */
public interface AirlineRepository extends JpaRepository<Airline, Long> {
    Optional<Airline> findAirlineByAirlineCode(String airlineCode);
}
