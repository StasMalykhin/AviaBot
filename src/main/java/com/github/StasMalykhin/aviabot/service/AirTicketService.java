package com.github.StasMalykhin.aviabot.service;

import com.github.StasMalykhin.aviabot.entity.AirTicket;
import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.repository.AirTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Сохраняет, удаляет и ищет авиабилеты.
 *
 * @author Stanislav Malykhin
 */
@Service
@RequiredArgsConstructor
public class AirTicketService {
    private final AirTicketRepository airTicketRepository;

    public void save(AirTicket airTicket) {
        airTicketRepository.save(airTicket);
    }

    public List<AirTicket> findAllTickets() {
        return airTicketRepository.findAll();
    }

    public void addTicket(AppUser user, AirTicket airTicket) {
        user.addAirTicket(airTicket);
        airTicketRepository.save(airTicket);
    }

    public void removeTicket(AppUser user, AirTicket airTicket) {
        user.removeAirTicket(airTicket);
        airTicketRepository.delete(airTicket);
    }

    public List<AirTicket> findAllSubscriptionsByUser(AppUser user) {
        return airTicketRepository.findByAppUser(user);
    }

    public Optional<AirTicket> findTicketWithSameParametersInSubscriptions(AirTicket airTicket, AppUser user) {
        String nameCityDeparture = airTicket.getNameCityDeparture();
        String nameCityDestination = airTicket.getNameCityDestination();
        Date departureDay = airTicket.getDepartureDay();
        return airTicketRepository
                .findAirTicketByNameCityDepartureAndNameCityDestinationAndDepartureDayAndAppUser(
                        nameCityDeparture, nameCityDestination, departureDay, user);
    }

    public Optional<AirTicket> findTicketInSubscriptions(AirTicket airTicket,
                                                         AppUser user) {
        String flightNumber = airTicket.getFlightNumber();
        String nameCityDeparture = airTicket.getNameCityDeparture();
        String nameCityDestination = airTicket.getNameCityDestination();
        Date departureAt = airTicket.getDepartureAt();
        return airTicketRepository
                .findAirTicketByFlightNumberAndNameCityDepartureAndNameCityDestinationAndDepartureAtAndAppUser(
                        flightNumber, nameCityDeparture, nameCityDestination, departureAt, user);
    }
}
