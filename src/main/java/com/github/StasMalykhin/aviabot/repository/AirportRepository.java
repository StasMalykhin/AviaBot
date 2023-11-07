package com.github.StasMalykhin.aviabot.repository;

import com.github.StasMalykhin.aviabot.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Stanislav Malykhin
 */
public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findAirportByAirportCode(String airportCode);

    List<Airport> findAirportsByCountryCode(String countryCode);

}
