package com.github.StasMalykhin.aviabot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.StasMalykhin.aviabot.entity.Airline;
import com.github.StasMalykhin.aviabot.repository.AirlineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Загружает при запуске бота IATA коды авиакомпаний в БД.
 *
 * @author Stanislav Malykhin
 */
@Service
@Log4j
@RequiredArgsConstructor
public class AirlineService extends APIConnectionService {
    private final AirlineRepository airlineRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${travelpayouts.endpoint.iataAirlineCodes}")
    private String iataAirlineCodesURI;

    public boolean checkFullnessOfDatabase() {
        return airlineRepository.findAll().isEmpty();
    }

    public void fillDatabase() {
        log.info("IATA коды авиакомпаний загружены в БД");
        HttpEntity<HttpHeaders> request = createRequestWithHeaders();
        var response = restTemplate.exchange(iataAirlineCodesURI, HttpMethod.GET, request, String.class).getBody();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            log.error(e);
        }
        for (int i = 0; i < jsonNode.size(); i++) {
            airlineRepository.save(Airline.builder()
                    .airlineCode(jsonNode.get(i).get("code").asText())
                    .name(jsonNode.get(i).get("name").asText())
                    .nameTranslations(jsonNode.get(i).get("name_translations").get("en").asText())
                    .build());
        }
    }
}
