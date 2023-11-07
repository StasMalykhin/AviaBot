package com.github.StasMalykhin.aviabot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.StasMalykhin.aviabot.entity.Airport;
import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.entity.City;
import com.github.StasMalykhin.aviabot.exceptions.FoundWrongSearchResultException;
import com.github.StasMalykhin.aviabot.exceptions.NoSuchCountryException;
import com.github.StasMalykhin.aviabot.repository.AirportRepository;
import com.github.StasMalykhin.aviabot.repository.CityRepository;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ищет все города с аэропортами по названию страны.
 *
 * @author Stanislav Malykhin
 */
@Service
@Log4j
@RequiredArgsConstructor
public class AirportsBookService extends APIConnectionService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CityRepository cityRepository;
    private final AirportRepository airportRepository;
    private final CreatorMessages creatorMsg;
    private final MessageService messageService;
    @Value("${travelpayouts.endpoint.autocomplete}")
    private String autocompleteURI;
    private static final String NAME_COUNTRY_REGEX = "[а-яёА-ЯЁ]+[а-яёА-ЯЁ -]*";
    private static final String LIMIT_OF_NAMES_IN_ONE_MESSAGE_REGEX = "([а-яёА-ЯЁ\\w ()-]+\\n){1,100}";

    public List<PartialBotApiMethod<? extends Serializable>>
    findCitiesWithAirportByNameCountry(String message, AppUser user) {
        Pattern pattern = Pattern.compile(NAME_COUNTRY_REGEX);
        Matcher matcher = pattern.matcher(message);
        boolean formatOfEnteredCountryNameIsIncorrect = !matcher.matches();
        if (formatOfEnteredCountryNameIsIncorrect) {
            String text = messageService.getMessage("airportsBook.failed");
            return List.of(creatorMsg.createSendMessage(text, user));
        }
        List<String> results = getCountryNameAndCheckForErrorsOrLackOfResponse(message);
        if (results.size() > 1) {
            return createMessageWithAnswerOptions(results, user);
        } else {
            String queryResult = getListWithCitiesAndConvertIntoString(results);
            String text = messageService.getMessage("airportsBook.continueOrReturnToMenu");
            SendMessage messageWithContinueOrReturnToMenu = creatorMsg.createSendMessage(text, user);
            boolean numberCharsExceedsLimitInOneMessage = queryResult.length() > 4096;
            if (numberCharsExceedsLimitInOneMessage) {
                return splitMessageIntoParts(queryResult, user, messageWithContinueOrReturnToMenu);
            }
            return List.of(creatorMsg.createSendMessage(queryResult, user),
                    messageWithContinueOrReturnToMenu);
        }
    }

    private List<String> getCountryNameAndCheckForErrorsOrLackOfResponse(String message) {
        List<String> results = null;
        try {
            results = getCountrySearchResult(message);
        } catch (NoSuchCountryException | FoundWrongSearchResultException ex) {
            log.error(ex);
            String noSuchCountry = messageService.getMessage("airportsBook.noSuchCountry");
            results = Collections.singletonList(noSuchCountry);
        } catch (JsonProcessingException e) {
            log.error(e);
        }
        return results;
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    createMessageWithAnswerOptions(List<String> countries, AppUser user) {
        Iterator<String> itr = countries.iterator();
        StringBuilder text = new StringBuilder(
                messageService.getMessage("airportsBook.startOfListWithSelectionOfCountries"));
        while (itr.hasNext()) {
            text.append(itr.next()).append("\n");
        }
        text.append(messageService.getMessage("airportsBook.endOfListWithSelectionOfCountries"));
        return List.of(creatorMsg.createSendMessage(text.toString(), user));
    }

    private String getListWithCitiesAndConvertIntoString(List<String> results) {
        List<String> resultAfterErrorChecking = sendCityWithAirportSearchRequest(results);
        Iterator<String> itr = resultAfterErrorChecking.iterator();
        StringBuilder queryResult = new StringBuilder();
        while (itr.hasNext()) {
            queryResult.append(itr.next()).append("\n");
        }
        return queryResult.toString();
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    splitMessageIntoParts(String queryResult, AppUser user, SendMessage lastMessage) {
        Pattern pattern = Pattern.compile(LIMIT_OF_NAMES_IN_ONE_MESSAGE_REGEX);
        Matcher matcher = pattern.matcher(queryResult);
        List<PartialBotApiMethod<? extends Serializable>> bigMessage = new ArrayList<>();
        while (matcher.find()) {
            bigMessage.add(creatorMsg.createSendMessage(matcher.group(), user));
        }
        bigMessage.add(lastMessage);
        return bigMessage;
    }

    private List<String> sendCityWithAirportSearchRequest(List<String> results) {
        String result = results.get(0);
        if (result.equals("Не могу найти страну с таким названием. " +
                "Либо она не существует, либо в ней нет аэропорта.")) {
            return results;
        }
        Set<String> cityCodes = findListCityCodesByCountryCode(result);
        return findListCitiesWithAirport(cityCodes, result);
    }

    private List<String> getCountrySearchResult(String countryNamePart) throws JsonProcessingException,
            FoundWrongSearchResultException, NoSuchCountryException {
        HttpEntity<HttpHeaders> request = createRequestWithHeaders();
        String uri = UriComponentsBuilder.fromUriString(autocompleteURI)
                .queryParam("locale", "ru").queryParam("types[]", "country")
                .queryParam("term", countryNamePart).build().toUriString();
        var response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class).getBody();
        JsonNode jsonNode = objectMapper.readTree(response);
        ArrayList<String> results;
        if (jsonNode.isEmpty()) {
            throw new NoSuchCountryException("По запросу пользователя ничего найти не удалось");
        } else {
            results = convertJsonToListWithCountryNames(countryNamePart, jsonNode);
            boolean searchResultIsNotCorrect = results.size() == 0;
            if (searchResultIsNotCorrect) {
                throw new FoundWrongSearchResultException("Найденные страны не соответствуют запросу пользователя");
            } else if (results.size() == 1) {
                return Collections.singletonList(jsonNode.get(0).get("code").asText());
            }
        }
        return results;
    }

    private ArrayList<String> convertJsonToListWithCountryNames(String countryNamePart, JsonNode jsonNode) {
        ArrayList<String> results = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            String currentCountryName = jsonNode.get(i).get("name").asText().toLowerCase();
            String countryNamePartInLowercaseLetters = countryNamePart.toLowerCase();
            if (currentCountryName.startsWith(countryNamePartInLowercaseLetters)) {
                results.add(jsonNode.get(i).get("name").asText());
            }
        }
        return results;
    }

    private Set<String> findListCityCodesByCountryCode(String countryCode) {
        List<Airport> airports = airportRepository.findAirportsByCountryCode(countryCode);
        Set<String> cityCodes = new HashSet<>();
        for (Airport airport : airports) {
            cityCodes.add(airport.getCityCode());
        }
        return cityCodes;
    }

    private List<String> findListCitiesWithAirport(Set<String> airports, String countryCode) {
        List<City> allCitiesByCountry = cityRepository.findCitiesByCountryCode(countryCode);
        SortedSet<String> setWithNameCities = new TreeSet<>();
        for (City city : allCitiesByCountry) {
            for (String airport : airports) {
                boolean cityCodeFromListOfAirportsMatchesCodeFromListOfCities =
                        airport.equals(city.getCityCode());
                if (cityCodeFromListOfAirportsMatchesCodeFromListOfCities) {
                    boolean nameOfCityIsNotWrittenInRussian =
                            city.getName().equals("null");
                    if (nameOfCityIsNotWrittenInRussian) {
                        setWithNameCities.add(city.getNameTranslations());
                    } else {
                        setWithNameCities.add(city.getName());
                    }
                }
            }
        }
        return new ArrayList<>(setWithNameCities);
    }
}
