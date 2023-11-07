package com.github.StasMalykhin.aviabot.service;

import com.github.StasMalykhin.aviabot.entity.AirTicket;
import com.github.StasMalykhin.aviabot.util.DateConversion;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Преобразует билет из строки сообщения в AirTicket.
 *
 * @author Stanislav Malykhin
 */
@Service
@Log4j
@RequiredArgsConstructor
public class ConverterToAirTicket {
    private final DateConversion dateConversion;
    private static final String AIR_TICKET_CHARACTERISTICS_REGEX = ": ([\\wа-яёА-ЯЁ\\d .(),:-]+)";
    private static final String PRICE_OR_TIME_REGEX = "(\\d+)[а-я. ]+(\\d+)?";

    public AirTicket conversionToAirTicket(String message) {
        Pattern pattern = Pattern.compile(AIR_TICKET_CHARACTERISTICS_REGEX);
        Matcher matcher = pattern.matcher(message);
        List<String> airTicket = new ArrayList<>();
        while (matcher.find()) {
            airTicket.add(matcher.group(1));
        }
        return mappingWithTicket(airTicket);
    }

    private AirTicket mappingWithTicket(List<String> airTicket) {
        String price = airTicket.get(7);
        Pattern pattern = Pattern.compile(PRICE_OR_TIME_REGEX);
        Matcher matcher = pattern.matcher(price);
        boolean foundSomePrice = matcher.find();
        Integer minPrice = getFirstNumberFromString(matcher, foundSomePrice);

        return initTicket(airTicket, minPrice);
    }

    private AirTicket initTicket(List<String> airTicket, Integer minPrice) {
        AirTicket ticket = new AirTicket(airTicket.get(0), airTicket.get(1), airTicket.get(2),
                airTicket.get(3), airTicket.get(4), airTicket.get(5),
                airTicket.get(6), minPrice);

        boolean priceWasChanged = airTicket.size() > 12;
        String nameAirline;
        Date departureAt;
        Date arrivalAt;
        String time;
        if (priceWasChanged) {
            nameAirline = airTicket.get(9);
            departureAt = dateConversion.fromStringToDate("yyyy-MM-dd HH:mm", airTicket.get(10));
            arrivalAt = dateConversion.fromStringToDate("yyyy-MM-dd HH:mm", airTicket.get(11));
            time = airTicket.get(12);
        } else {
            nameAirline = airTicket.get(8);
            departureAt = dateConversion.fromStringToDate("yyyy-MM-dd HH:mm", airTicket.get(9));
            arrivalAt = dateConversion.fromStringToDate("yyyy-MM-dd HH:mm", airTicket.get(10));
            time = airTicket.get(11);
        }
        setDateAndTimeAndNameAirline(ticket, time, nameAirline, departureAt, arrivalAt);
        return ticket;
    }

    private void setDateAndTimeAndNameAirline(AirTicket ticket, String time,
                                              String nameAirline, Date departureAt, Date arrivalAt) {
        Pattern pattern = Pattern.compile(PRICE_OR_TIME_REGEX);
        Matcher matcher = pattern.matcher(time);
        boolean foundSomeNumber = matcher.find();
        Integer countHour = getFirstNumberFromString(matcher, foundSomeNumber);
        Integer countMinute = getSecondNumberFromString(matcher, foundSomeNumber);
        Integer flightTime = countHour * 60 + countMinute;

        ticket.setFlightTime(flightTime);
        ticket.setNameAirline(nameAirline);
        ticket.setDepartureAt(departureAt);
        ticket.setDepartureDay(dateConversion.resetTime(departureAt));
        ticket.setArrivalAt(arrivalAt);
    }

    private Integer getFirstNumberFromString(Matcher matcher, boolean foundSomeNumber) {
        if (foundSomeNumber) {
            return Integer.valueOf(matcher.group(1));
        }
        return null;
    }

    private Integer getSecondNumberFromString(Matcher matcher, boolean foundSomeNumber) {
        if (foundSomeNumber) {
            return (matcher.group(2) == null) ? null : Integer.valueOf(matcher.group(2));
        }
        return null;
    }
}
