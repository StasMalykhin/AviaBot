package com.github.StasMalykhin.aviabot.util;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Преобразует строку в Date и обратно, прибавляет к Date минуты,
 * а также обнуляет у Date часы/минуты/секунды/миллисекунды.
 *
 * @author Stanislav Malykhin
 */
@Component
@Log4j
public class DateConversion {
    public Date fromStringToDate(String pattern, String departureAt) {
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern(pattern);
        Date departureDate = null;
        try {
            departureDate = format.parse(departureAt);
        } catch (ParseException e) {
            log.error(e);
        }
        return departureDate;
    }

    public Date resetTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public Date addMinutesToDate(Date departureDate, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(departureDate);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public String fromDateToString(String pattern, Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);

    }
}
