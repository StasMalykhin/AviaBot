package com.github.StasMalykhin.aviabot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.StasMalykhin.aviabot.entity.Airport;
import com.github.StasMalykhin.aviabot.repository.AirportRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Загружает при запуске бота IATA коды аэропортов в БД.
 *
 * @author Stanislav Malykhin
 */
@Service
@Log4j
@RequiredArgsConstructor
public class CompleteDatabaseWithAirportsService extends APIConnectionService {
    private final AirportRepository airportRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${travelpayouts.endpoint.iataAirportCodes}")
    private String iataAirportCodesURI;

    @PostConstruct
    public void uploadAirportsInBD() {
        log.info("IATA коды аэропортов загружены в БД");
        HttpEntity<HttpHeaders> request = createRequestWithHeaders();
        var response = restTemplate.exchange(iataAirportCodesURI, HttpMethod.GET, request, String.class).getBody();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            log.error(e);
        }
        for (int i = 0; i < jsonNode.size(); i++) {
            if (jsonNode.get(i).get("iata_type").asText().equals("airport")) {
                airportRepository.save(Airport.builder()
                        .cityCode(jsonNode.get(i).get("city_code").asText())
                        .countryCode(jsonNode.get(i).get("country_code").asText())
                        .airportCode(jsonNode.get(i).get("code").asText())
                        .name(jsonNode.get(i).get("name").asText())
                        .nameTranslations(jsonNode.get(i).get("name_translations").get("en").asText())
                        .build());
            }
        }
    }
}
