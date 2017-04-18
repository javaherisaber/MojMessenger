import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Mahdi on 9/1/2017.
 */
public class Time {

    public static String getLocalTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
        Date date = new Date();
        String time = dateFormat.format(date);
        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
        double zone = tz.getOffset(now.getTime()) / 3600000.0;
        return time + "_" + zone;
    }

    public static void main(String[] args) {
        String v = getLocalTimeStamp();
        System.out.println(v);
    }
}
