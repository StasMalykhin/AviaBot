package com.github.StasMalykhin.aviabot.repository;

import com.github.StasMalykhin.aviabot.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Stanislav Malykhin
 */
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findCityByCityCode(String cityCode);

    List<City> findCitiesByCountryCode(String countryCode);
}
