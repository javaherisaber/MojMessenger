package util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Mahdi on 9/1/2017.
 */
public class Time {

    public static String getUTCTimestamp() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        return utc.format(DateTimeFormatter.ofPattern("yyyy/MM/dd_HH:mm"));
    }

}
