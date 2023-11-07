package com.github.StasMalykhin.aviabot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.StasMalykhin.aviabot.entity.*;
import com.github.StasMalykhin.aviabot.entity.enums.Command;
import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import com.github.StasMalykhin.aviabot.exceptions.FoundWrongSearchResultException;
import com.github.StasMalykhin.aviabot.exceptions.NoSuchCityException;
import com.github.StasMalykhin.aviabot.repository.AirlineRepository;
import com.github.StasMalykhin.aviabot.repository.AirportRepository;
import com.github.StasMalykhin.aviabot.repository.CityRepository;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import com.github.StasMalykhin.aviabot.util.DateConversion;
import com.github.StasMalykhin.aviabot.util.KeyboardMarkup;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Принимает от пользователя параметры (город отправления/назначения и дата отправления) поиска
 * и ищет самый дешевый авиабилет.
 *
 * @author Stanislav Malykhin
 */
@Service
@Log4j
@RequiredArgsConstructor
public class SearchService extends APIConnectionService {
    private final CityRepository cityRepository;
    private final AirportRepository airportRepository;
    private final AirlineRepository airlineRepository;
    private final AirTicketSearchService airTicketSearchService;
    private final MessageService messageService;
    private final KeyboardMarkup keyboardMarkup;
    private final DateConversion dateConversion;
    private final CreatorMessages creatorMsg;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${travelpayouts.endpoint.searchForCheapestAirTicketForCertainDate}")
    private String searchForCheapestAirTicketURI;
    @Value("${travelpayouts.endpoint.autocomplete}")
    private String autocompleteURI;
    @Value("${domainAviasales}")
    private String aviasalesURI;
    private static final String CODE_CITY_FROM_INLINE_KEYBOARD_REGEX = "/([\\wа-яёА-ЯЁ]{3})$";
    private static final String NAME_CITY_REGEX = "[а-яёА-ЯЁ]+[а-яёА-ЯЁ -]*";
    private static final String DATE_FROM_INPUT_MESSAGE = "\\d{4}-\\d{2}-\\d{2}";

    public List<PartialBotApiMethod<? extends Serializable>>
    selectionCity(String message, AppUser user, InlineKeyboardMarkup keyboardWithButtonConfirmation) {
        Pattern pattern = Pattern.compile(CODE_CITY_FROM_INLINE_KEYBOARD_REGEX);
        Matcher matcher = pattern.matcher(message);
        boolean cityCodeIsBackFromButton = matcher.find();
        if (!cityCodeIsBackFromButton) {
            pattern = Pattern.compile(NAME_CITY_REGEX);
            matcher = pattern.matcher(message);
            boolean nameOfCityIsNotEnteredCorrectly = !matcher.matches();
            if (nameOfCityIsNotEnteredCorrectly) {
                String text = messageService.getMessage("searchAirTickets.failed");
                return List.of(creatorMsg.createSendMessage(text, user));
            }
        }
        List<CityWithAirCommunication> results = getCityNameAndCheckForErrorsOrLackOfResponse(message,
                cityCodeIsBackFromButton);
        if (results.size() > 1) {
            return createMessageWithAnswerOptions(results, user);
        } else {
            return processSingleSearchResult(user, results, keyboardWithButtonConfirmation);
        }
    }

    private List<CityWithAirCommunication>
    getCityNameAndCheckForErrorsOrLackOfResponse(String message, boolean cityCodeIsBackFromButton) {
        List<CityWithAirCommunication> results = null;
        try {
            results = getCitySearchResult(message, cityCodeIsBackFromButton);
        } catch (NoSuchCityException | FoundWrongSearchResultException ex) {
            log.error(ex);
            results = Collections.emptyList();
        } catch (JsonProcessingException e) {
            log.error(e);
        }
        return results;
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    createMessageWithAnswerOptions(List<CityWithAirCommunication> cities, AppUser user) {
        LinkedHashMap<String, String> buttonsOfCities = new LinkedHashMap<>();
        Iterator<CityWithAirCommunication> itr = cities.iterator();
        String text = messageService.getMessage("searchAirTickets.titleOfListWithSelectionOfCities");
        while (itr.hasNext()) {
            CityWithAirCommunication city = itr.next();
            String button = "г. " + city.getNameCity() + " (" + city.getNameCountry() + ") ";
            buttonsOfCities.put("/" + city.getCodeCity(), button);
        }
        InlineKeyboardMarkup inlineKeyboard = keyboardMarkup.createInlineKeyboardWithOneColumn(buttonsOfCities);
        return List.of(creatorMsg.createSendMessageWithKeyboard(text, user, inlineKeyboard));
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    processSingleSearchResult(AppUser user, List<CityWithAirCommunication> results,
                              InlineKeyboardMarkup keyboardWithButtonConfirmation) {
        boolean searchQueryDidNotYieldAnySatisfactoryResults = results.isEmpty();
        if (searchQueryDidNotYieldAnySatisfactoryResults) {
            String messageWithBug = messageService.getMessage("searchAirTickets.noSuchCity");
            return List.of(creatorMsg.createSendMessage(messageWithBug, user));
        } else {
            String code = results.stream().findFirst().get().getCodeCity();
            String cityName = results.stream().findFirst().get().getNameCity();
            Optional<AirTicketSearch> airTicketSearch = airTicketSearchService.findByAppUser(user);
            if (user.getState().equals(UserState.ENTER_DESTINATION_CITY_NAME_WHEN_SEARCHING_FOR_AIR_TICKETS)) {
                return checkNameOfDestinationCityForMatchWithDepartureCityAndDisplayMessageToUser(
                        code, cityName, airTicketSearch, user, keyboardWithButtonConfirmation);
            } else {
                saveSelectedDepartureCityToDB(code, user, airTicketSearch);
            }
            log.info("По введенному пользователем " + user.getUsername() +
                    " названию был найден город отправления \"" + cityName + "\".");

            String text = messageService
                    .getMessage("searchAirTickets.confirmationOfDepartureCity", cityName);
            return List.of(creatorMsg.createSendMessageWithKeyboard(text, user,
                    keyboardWithButtonConfirmation));
        }
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    checkNameOfDestinationCityForMatchWithDepartureCityAndDisplayMessageToUser(
            String code, String cityName,
            Optional<AirTicketSearch> airTicketSearch, AppUser user,
            InlineKeyboardMarkup keyboardWithButtonConfirmation) {
        boolean pointDepartureIsEqualToPointDestination =
                airTicketSearch.get().getDepartureCityCode().equals(code);
        if (pointDepartureIsEqualToPointDestination) {
            String text = messageService.getMessage("searchAirTickets.pointDepartureIsEqualToPointDestination");
            return List.of(creatorMsg.createSendMessage(text, user));
        } else {
            airTicketSearch.get().setDestinationCityCode(code);
            airTicketSearchService.save(airTicketSearch.get());

            log.info("По введенному пользователем " + user.getUsername() +
                    " названию был найден город назначения \"" + cityName + "\".");

            String text = messageService
                    .getMessage("searchAirTickets.confirmationOfDestinationCity", cityName);
            return List.of(creatorMsg.createSendMessageWithKeyboard(text, user,
                    keyboardWithButtonConfirmation));
        }
    }

    private void saveSelectedDepartureCityToDB(String code, AppUser user,
                                               Optional<AirTicketSearch> airTicketSearch) {
        boolean needToUpdateDepartureCityInAirTicketSearch = airTicketSearch.isPresent();
        if (needToUpdateDepartureCityInAirTicketSearch) {
            airTicketSearch.get().setDepartureCityCode(code);
            airTicketSearchService.save(airTicketSearch.get());
        } else {
            airTicketSearchService.save(AirTicketSearch.builder()
                    .appUser(user)
                    .departureCityCode(code)
                    .build());
        }
    }

    public List<PartialBotApiMethod<? extends Serializable>>
    selectionDateAndSearchTickets(String enteredDate, AppUser user,
                                  ReplyKeyboardMarkup keyboardWithButtonsAgainAndBack) {
        boolean ticketSearchWasConducted = airTicketSearchService.findByAppUser(user).isEmpty();
        if (ticketSearchWasConducted) {
            String text = messageService.getMessage("searchAirTickets.pointerToButtons");
            return List.of(creatorMsg.createSendMessageWithKeyboard(text, user,
                    keyboardWithButtonsAgainAndBack));
        } else {
            Pattern pattern = Pattern.compile(DATE_FROM_INPUT_MESSAGE);
            Matcher matcher = pattern.matcher(enteredDate);
            boolean dateIsNotEnteredCorrectly = !matcher.matches();
            if (dateIsNotEnteredCorrectly) {
                String text = messageService.getMessage("searchAirTickets.invalidDate");
                return List.of(creatorMsg.createSendMessage(text, user));
            } else {
                return checkExistenceOfDateAndSearchTickets(enteredDate, user, keyboardWithButtonsAgainAndBack);
            }
        }
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    checkExistenceOfDateAndSearchTickets(String enteredDate, AppUser user,
                                         ReplyKeyboardMarkup keyboardWithButtonsAgainAndBack) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT);
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(enteredDate, formatter);
        } catch (DateTimeParseException ex) {
            log.error(ex);
            String text = messageService.getMessage("searchAirTickets.noSuchDate");
            return List.of(creatorMsg.createSendMessage(text, user));
        }
        LocalDate now = LocalDate.now();
        boolean dateIsPast = now.compareTo(selectedDate) > 0;
        if (dateIsPast) {
            String text = messageService.getMessage("searchAirTickets.dateIsPast");
            return List.of(creatorMsg.createSendMessage(text, user));
        } else {
            return searchTickets(enteredDate, user, keyboardWithButtonsAgainAndBack);
        }
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    searchTickets(String enteredDate, AppUser user, ReplyKeyboardMarkup keyboardWithButtonsAgainAndBack) {
        Optional<AirTicketSearch> airTicketSearch = airTicketSearchService.findByAppUser(user);
        airTicketSearch.get().setDepartureAt(enteredDate);
        Optional<AirTicket> ticket = Optional.ofNullable(
                findAirTicket(airTicketSearch.get().getDepartureCityCode(),
                        airTicketSearch.get().getDestinationCityCode(),
                        airTicketSearch.get().getDepartureAt()));
        boolean ticketNotFound = ticket.isEmpty();
        if (ticketNotFound) {
            String text = messageService.getMessage("searchAirTickets.ticketsNotFound");
            return List.of(creatorMsg.createSendMessageWithKeyboard(text, user,
                    keyboardWithButtonsAgainAndBack));
        } else {
            AirTicket realTicket = ticket.get();
            return outputFoundTicket(user, airTicketSearch, keyboardWithButtonsAgainAndBack, realTicket);
        }
    }


    private List<PartialBotApiMethod<? extends Serializable>>
    outputFoundTicket(AppUser user, Optional<AirTicketSearch> airTicketSearch,
                      ReplyKeyboardMarkup keyboardWithButtonsAgainAndBack,
                      AirTicket ticket) {

        log.info("По заданным критериям пользователя " + user.getUsername() +
                " был найден авиабилет.");

        airTicketSearchService.delete(airTicketSearch.get());

        List<PartialBotApiMethod<? extends Serializable>> messageWithTicket
                = new ArrayList<>();
        addTicketInMessage(messageWithTicket, ticket, user);

        String lastText = messageService.getMessage("searchAirTickets.endOfSearch");
        messageWithTicket.add(creatorMsg.createSendMessageWithKeyboard(lastText, user,
                keyboardWithButtonsAgainAndBack));
        return messageWithTicket;
    }

    private void addTicketInMessage(List<PartialBotApiMethod<? extends Serializable>> messageWithTicket,
                                    AirTicket ticket, AppUser user) {
        String price = ticket.getFirstMinPrice() + " руб.";
        String departureDate =
                dateConversion.fromDateToString("yyyy-MM-dd HH:mm", ticket.getDepartureAt());
        String arrivalDate =
                dateConversion.fromDateToString("yyyy-MM-dd HH:mm", ticket.getArrivalAt());
        InlineKeyboardMarkup inlineKeyboard = fillInButtonsAndReturnKeyboard(ticket);

        messageWithTicket.add(
                creatorMsg.createMessageWithAirTicket(user, inlineKeyboard,
                        ticket, price, departureDate, arrivalDate));
    }

    private InlineKeyboardMarkup fillInButtonsAndReturnKeyboard(AirTicket ticket) {
        LinkedHashMap<String, String> buttons = new LinkedHashMap<>();
        buttons.put(ticket.getURL(), "Купить");
        buttons.put(Command.SUBSCRIBE_TO_TICKET.toString(), "Подписаться");
        return keyboardMarkup
                .createInlineKeyboardWithButtonsBuyAndSubscribe(buttons);
    }

    public AirTicket findAirTicket(String departureCityCode, String destinationCityCode,
                                   String departureAt) {
        HttpEntity<HttpHeaders> request = createRequestWithHeaders();
        String uri = UriComponentsBuilder
                .fromUriString(searchForCheapestAirTicketURI)
                .queryParam("currency", "rub")
                .queryParam("origin", departureCityCode)
                .queryParam("destination", destinationCityCode)
                .queryParam("departure_at", departureAt)
                .queryParam("direct", "false")
                .queryParam("one_way", "true")
                .queryParam("market", "ru")
                .queryParam("limit", 1000)
                .queryParam("sorting", "price")
                .build().toUriString();
        var response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class).getBody();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response).get("data");
        } catch (JsonProcessingException e) {
            log.error(e);
        }
        return conversionToTicket(jsonNode);
    }

    private List<CityWithAirCommunication> getCitySearchResult(String cityNamePart,
                                                               boolean cityCodeIsBackFromButton)
            throws JsonProcessingException, FoundWrongSearchResultException, NoSuchCityException {

        HttpEntity<HttpHeaders> request = createRequestWithHeaders();
        String uri = UriComponentsBuilder
                .fromUriString(autocompleteURI)
                .queryParam("locale", "ru")
                .queryParam("types[]", "city")
                .queryParam("term", cityNamePart)
                .build().toUriString();
        var response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class).getBody();
        JsonNode jsonNode = objectMapper.readTree(response);
        List<CityWithAirCommunication> results = new ArrayList<>();
        boolean noCitiesWithSuchPartialNameWereFound = jsonNode.isEmpty();
        if (noCitiesWithSuchPartialNameWereFound) {
            throw new NoSuchCityException("По запросу пользователя ничего найти не удалось");
        } else {
            addCityWithAirCommunicationToList(cityNamePart, cityCodeIsBackFromButton, results, jsonNode);
        }
        return results;
    }

    private void
    addCityWithAirCommunicationToList(String cityNamePart, boolean cityCodeIsBackFromButton,
                                      List<CityWithAirCommunication> results,
                                      JsonNode jsonNode) throws FoundWrongSearchResultException {
        for (int i = 0; i < jsonNode.size(); i++) {
            String currentCityName = jsonNode.get(i).get("name").asText().toLowerCase();
            String cityNamePartInLowercaseLetters = cityNamePart.toLowerCase();
            boolean matchWasFoundForCityCode = cityCodeIsBackFromButton &
                    cityNamePart.substring(1).equals(jsonNode.get(i).get("code").asText());
            boolean matchWasFoundForPartOfNameOfCity = currentCityName.startsWith(cityNamePartInLowercaseLetters);
            if (matchWasFoundForPartOfNameOfCity || matchWasFoundForCityCode) {
                results.add(CityWithAirCommunication.builder()
                        .codeCity(jsonNode.get(i).get("code").asText())
                        .nameCity(jsonNode.get(i).get("name").asText())
                        .nameCountry(jsonNode.get(i).get("country_name").asText())
                        .build());
            }
        }
        boolean searchResultIsNotCorrect = results.size() == 0;
        if (searchResultIsNotCorrect) {
            throw new FoundWrongSearchResultException("Найденные города не соответствуют запросу пользователя");
        }
    }

    private AirTicket conversionToTicket(JsonNode jsonNode) {
        boolean searchResultWasNotFound = jsonNode.isEmpty();
        if (searchResultWasNotFound) {
            return null;
        }
        JsonNode jsonNodeWithTicket = jsonNode.get(0);
        String flightNumber = jsonNodeWithTicket.get("flight_number").asText();
        Date departureDate = dateConversion.fromStringToDate("yyyy-MM-dd'T'HH:mm:ss",
                jsonNodeWithTicket.get("departure_at").asText());
        int flightTime = jsonNodeWithTicket.get("duration_to").asInt();
        Date arrivalDate = dateConversion.addMinutesToDate(departureDate, flightTime);

        return initializeTicketAndReturn(jsonNodeWithTicket, flightNumber,
                departureDate, arrivalDate, flightTime);
    }

    private AirTicket initializeTicketAndReturn(JsonNode jsonNodeWithTicket, String flightNumber,
                                                Date departureDate, Date arrivalDate, int flightTime) {
        return AirTicket.builder()
                .flightNumber(flightNumber)
                .nameCityDeparture(findNameOfCity(jsonNodeWithTicket.get("origin").asText()))
                .codeCityDeparture(jsonNodeWithTicket.get("origin").asText())
                .nameCityDestination(findNameOfCity(jsonNodeWithTicket.get("destination").asText()))
                .codeCityDestination(jsonNodeWithTicket.get("destination").asText())
                .departureAirport(findAirportName(jsonNodeWithTicket.get("origin_airport").asText()))
                .destinationAirport(findAirportName(jsonNodeWithTicket.get("destination_airport").asText()))
                .firstMinPrice(jsonNodeWithTicket.get("price").asInt())
                .nameAirline(findAirlineName(jsonNodeWithTicket.get("airline").asText()))
                .departureAt(departureDate)
                .departureDay(dateConversion.resetTime(departureDate))
                .arrivalAt(arrivalDate)
                .flightTime(flightTime)
                .URL(aviasalesURI + jsonNodeWithTicket.get("link").asText())
                .build();
    }

    private String findNameOfCity(String cityCode) {
        Optional<City> city = cityRepository.findCityByCityCode(cityCode);
        boolean nameOfThisCodeWasNotFoundInDB = city.isEmpty();
        if (nameOfThisCodeWasNotFoundInDB) {
            return cityCode;
        }
        boolean nameIsNotWrittenInRussian = city.get().getName().equals("null");
        if (nameIsNotWrittenInRussian) {
            return city.get().getNameTranslations();
        } else {
            return city.get().getName();
        }
    }

    private String findAirportName(String airportCode) {
        Optional<Airport> airport = airportRepository.findAirportByAirportCode(airportCode);
        boolean nameOfThisCodeWasNotFoundInDB = airport.isEmpty();
        if (nameOfThisCodeWasNotFoundInDB) {
            return airportCode;
        }
        boolean nameIsNotWrittenInRussian = airport.get().getName().equals("null");
        if (nameIsNotWrittenInRussian) {
            return airport.get().getNameTranslations();
        } else {
            return airport.get().getName();
        }
    }

    private String findAirlineName(String airlineCode) {
        Optional<Airline> airline = airlineRepository.findAirlineByAirlineCode(airlineCode);
        boolean nameOfThisCodeWasNotFoundInDB = airline.isEmpty();
        if (nameOfThisCodeWasNotFoundInDB) {
            return airlineCode;
        }
        boolean nameIsNotWrittenInRussian = airline.get().getName().equals("null");
        if (nameIsNotWrittenInRussian) {
            return airline.get().getNameTranslations();
        } else {
            return airline.get().getName();
        }
    }
}
