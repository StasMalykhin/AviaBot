package com.github.StasMalykhin.aviabot.service;

import com.github.StasMalykhin.aviabot.entity.AirTicketSearch;
import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.repository.AirTicketSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сохраняет, удаляет и ищет параметры поиска авиабилетов.
 *
 * @author Stanislav Malykhin
 */
@Service
@RequiredArgsConstructor
public class AirTicketSearchService {
    private final AirTicketSearchRepository airTicketSearchRepository;

    public void save(AirTicketSearch airTicketSearch) {
        airTicketSearchRepository.save(airTicketSearch);
    }

    public Optional<AirTicketSearch> findByAppUser(AppUser user) {
        return airTicketSearchRepository.findByAppUser(user);
    }

    public void delete(AirTicketSearch airTicketSearch) {
        airTicketSearchRepository.delete(airTicketSearch);
    }

    public void removeAirTicketSearch(AppUser user) {
        Optional<AirTicketSearch> airTicketSearch = airTicketSearchRepository.findByAppUser(user);
        airTicketSearch.ifPresent(airTicketSearchRepository::delete);
    }
}
