package top.panson.common.toolkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

/**
 * date and time util
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {

    /**
     * get time zone for this JVM
     */
    private static final TimeZone TIME_ZONE = TimeZone.getDefault();

    public static final String NORM_DATE_PATTERN = "yyyy-MM-dd";

    public static final String NORM_TIME_PATTERN = "HH:mm:ss";

    public static final String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * represented by this <tt>Date</tt> object.
     *
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * represented by this date.
     */
    public static long getTime(LocalDateTime date) {
        return getTime(date, TIME_ZONE.toZoneId());
    }

    public static long getTime(LocalDateTime date, ZoneId zoneId) {
        return date.atZone(zoneId).toInstant().toEpochMilli();

    }

    /**
     * modify format to date
     *
     * @param date            date
     * @param normTimePattern PATTERN
     * @return String
     */
    public static String format(Date date, String normTimePattern) {
        SimpleDateFormat zoneFormat = new SimpleDateFormat(normTimePattern);
        return zoneFormat.format(date);

    }
}
