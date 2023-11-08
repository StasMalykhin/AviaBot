package com.github.StasMalykhin.aviabot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Log4j
@Component
@RequiredArgsConstructor
public class DataBaseInit implements ApplicationRunner {
    private final AirlineService airlineService;
    private final AirportService airportService;
    private final CityService cityService;

    @Override
    public void run(ApplicationArguments args) {
        boolean databaseWithAirlinesIsNotFull = airlineService.checkFullnessOfDatabase();
        if (databaseWithAirlinesIsNotFull) {
            try {
                airlineService.fillDatabase();
            } catch (Exception e) {
                log.error(String.format("Cannot fill database with airlines. Error=%s", e.getMessage()));
            }
        }
        boolean databaseWithAirportsIsNotFull = airportService.checkFullnessOfDatabase();
        if (databaseWithAirportsIsNotFull) {
            try {
                airportService.fillDatabase();
            } catch (Exception e) {
                log.error(String.format("Cannot fill database with airports. Error=%s", e.getMessage()));
            }
        }
        boolean databaseWithCitiesIsNotFull = cityService.checkFullnessOfDatabase();
        if (databaseWithCitiesIsNotFull) {
            try {
                cityService.fillDatabase();
            } catch (Exception e) {
                log.error(String.format("Cannot fill database with cities. Error=%s", e.getMessage()));
            }
        }
    }
}
