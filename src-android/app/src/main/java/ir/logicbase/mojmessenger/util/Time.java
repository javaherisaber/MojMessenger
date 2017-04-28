package ir.logicbase.mojmessenger.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Mahdi on 7/15/2017.
 * everything needed with local time
 */

public class Time {

    public static String getLocalTimestamp() {
        DateFormat now = new SimpleDateFormat("yyyy/MM/dd_HH:mm");
        return convertLocalToUTC(now.format(Calendar.getInstance().getTime()));
    }

    public static String getConversationMessageLabel(String timestamp) {
        try {
            String localZoneTimestamp = convertUTCtoLocal(timestamp);
            String[] rootParts = localZoneTimestamp.split("_");
            return rootParts[1];
        } catch (Exception e) {
            return timestamp;
        }
    }

    /**
     * @param timestamp 2017/09/02_15:50
     */
    public static String getLastSeenLabel(String timestamp) {
        try {
            String localZoneTimestamp = convertUTCtoLocal(timestamp);
            Calendar serverDateTime = convertTimeToCalendar(localZoneTimestamp);

            JalaliCalendar serverCalendar = gregorianToJalali(serverDateTime);
            JalaliCalendar localCalendar = gregorianToJalali(Calendar.getInstance());
            if (localCalendar.getMonth() == serverCalendar.getMonth()) {
                if (localCalendar.getYear() == serverCalendar.getYear()
                        && localCalendar.getMonth() == serverCalendar.getMonth()
                        && localCalendar.getDay() == serverCalendar.getDay()) {
                    return calculateTodayTimeLabel(serverDateTime);
                }
                return calculateDateLabel(serverCalendar) + " در " +
                        serverDateTime.get(Calendar.HOUR_OF_DAY) + ":" + serverDateTime.get(Calendar.MINUTE);
            } else {
                return calculateDateLabel(serverCalendar);
            }
        } catch (Exception e) {
            return timestamp;
        }
    }

    public static String getChatListMessageLabel(String timestamp) {
        try {
            String localZoneTimestamp = convertUTCtoLocal(timestamp);
            Calendar serverDateTime = convertTimeToCalendar(localZoneTimestamp);

            JalaliCalendar serverCalendar = gregorianToJalali(serverDateTime);
            JalaliCalendar localCalendar = gregorianToJalali(Calendar.getInstance());
            if (localCalendar.getYear() == serverCalendar.getYear()
                    && localCalendar.getMonth() == serverCalendar.getMonth()
                    && localCalendar.getDay() == serverCalendar.getDay()) {
                return serverDateTime.get(Calendar.HOUR_OF_DAY) + ":" + serverDateTime.get(Calendar.MINUTE);
            } else {
                return calculateDateLabel(serverCalendar);
            }
        } catch (Exception e) {
            return timestamp;
        }
    }

    private static String convertUTCtoLocal(String timestamp) {
        try {
            Calendar c = convertTimeToCalendar(timestamp);
            // convert server time to local timezone
            int timezoneOffsetMinutes = (int) (getLocalTimeZone() * 60);
            c.add(Calendar.MINUTE, timezoneOffsetMinutes);
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd_HH:mm");
            return format.format(c.getTime());
        } catch (Exception e) {
            return timestamp;
        }
    }

    private static String convertLocalToUTC(String timestamp) {
        try {
            Calendar c = convertTimeToCalendar(timestamp);
            // convert server time to local timezone
            int timezoneOffsetMinutes = (int) (getLocalTimeZone() * 60);
            c.add(Calendar.MINUTE, -timezoneOffsetMinutes);
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd_HH:mm");
            return format.format(c.getTime());
        } catch (Exception e) {
            return timestamp;
        }
    }

    /**
     * @return Local TimeZone in hour (eg. 3.5 or -4.5)
     */
    private static double getLocalTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
        return tz.getOffset(now.getTime()) / 3600000.0;
    }

    private static Calendar convertTimeToCalendar(String timestamp) {
        String[] rootParts = timestamp.split("_");
        String date = rootParts[0];
        String time = rootParts[1];
        String[] dateParts = date.split("/");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);
        String[] clockParts = time.split(":");
        int hour = Integer.parseInt(clockParts[0]);
        int minute = Integer.parseInt(clockParts[1]);
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute);  // month is 0 base (we make it 1 base for better work with Jalali)
        return c;
    }

    private static String calculateTodayTimeLabel(Calendar serverTime) {
        Calendar localTime = Calendar.getInstance();
        if (localTime.get(Calendar.HOUR_OF_DAY) > serverTime.get(Calendar.HOUR_OF_DAY)) {
            return (localTime.get(Calendar.HOUR_OF_DAY) - serverTime.get(Calendar.HOUR_OF_DAY)) + " ساعت پیش";
        } else {
            if (localTime.get(Calendar.MINUTE) - serverTime.get(Calendar.MINUTE) == 0 ||
                    serverTime.get(Calendar.MINUTE) - localTime.get(Calendar.MINUTE) > 0) {
                return "لحظاتی پیش";
            } else {
                return (localTime.get(Calendar.MINUTE) - serverTime.get(Calendar.MINUTE)) + " دقیقه پیش";
            }
        }
    }

    private static String calculateDateLabel(JalaliCalendar serverCalendar) {
        Calendar c = Calendar.getInstance();
        JalaliCalendar localCalendar = gregorianToJalali(c);
        if (localCalendar.getYear() > serverCalendar.getYear()) {
            return "سال " + serverCalendar.getYear();
        } else if (localCalendar.getMonth() > serverCalendar.getMonth()) {
            return serverCalendar.getMonthString() + " ماه";
        } else if (localCalendar.getDay() - serverCalendar.getDay() < 7) {
            if (localCalendar.getDay() == serverCalendar.getDay() + 1) {
                return "دیروز";
            } else if (localCalendar.getDayOfWeek() > serverCalendar.getDayOfWeek()) {
                return serverCalendar.getDayOfWeekString();
            } else {
                return serverCalendar.getDay() + " " + serverCalendar.getMonthString();
            }
        } else {
            return serverCalendar.getDay() + " " + serverCalendar.getMonthString();
        }
    }

    private static JalaliCalendar gregorianToJalali(Calendar c) {
        return new JalaliCalendar(new GregorianCalendar(
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE)));
    }
}
