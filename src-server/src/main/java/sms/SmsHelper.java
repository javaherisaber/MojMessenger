package sms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import util.RESTFunctions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by Mahdi on 8/25/2017.
 * send sms
 */
public class SmsHelper {

    private final static String URL = "http://rest.payamak-panel.com/api/SendSMS/SendSMS";
    private final static String USER_NAME = "Please Prepare your own sms server";
    private final static String PASSWORD = "Please Prepare your own sms server";
    private final static String FROM_NUMBER = "Please Prepare your own sms server";
    public final static String TEXT_SEND_OTP = "کد تایید شما در موج مسنجر : ";

    public static boolean sendOTPSms(String phone, String text){
        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        String params = String.format("username=%s&password=%s&to=%s" +
                "&from=%s&text=%s&isflash=false", USER_NAME, PASSWORD, phone, FROM_NUMBER, text);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

        RESTFunctions rf = new RESTFunctions();
        String output = rf.getRawResponse(RESTFunctions.RequestType.POST, URL, params, headers);
        /*
        sample output :
        {"Value":"4788913819552000978","RetStatus":1,"StrRetStatus":"Ok"}
         */

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(output);
        JsonObject objectRoot = element.getAsJsonObject();
        int status = objectRoot.get("RetStatus").getAsInt();
        return (status == 1);
    }
}
