package com.github.StasMalykhin.aviabot.repository;

import com.github.StasMalykhin.aviabot.entity.AirTicket;
import com.github.StasMalykhin.aviabot.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Stanislav Malykhin
 */
public interface AirTicketRepository extends JpaRepository<AirTicket, Long> {
    List<AirTicket> findByAppUser(AppUser appUser);

    Optional<AirTicket>
    findAirTicketByFlightNumberAndNameCityDepartureAndNameCityDestinationAndDepartureAtAndAppUser(
            String flightNumber,
            String nameCityDeparture,
            String nameCityDestination,
            Date departureAt, AppUser appUser);

    Optional<AirTicket>
    findAirTicketByNameCityDepartureAndNameCityDestinationAndDepartureDayAndAppUser(String nameCityDeparture,
                                                                                    String nameCityDestination,
                                                                                    Date departureDay, AppUser appUser);
}
