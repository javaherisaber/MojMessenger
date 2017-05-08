package util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RESTFunctions {


    public enum RequestType {
        POST,
        GET
    }

    private String RESTRequest(RequestType type, String inputUrl, String params, HashMap<String,String> headers){

        final int CONNECTION_TIMEOUT = 15000;
        final int READ_TIMEOUT = 15000;

        URL url;
        try {
            url = new URL(inputUrl);
        } catch (MalformedURLException e) {
            return null;
        }

        HttpURLConnection con = null ;
        try {

            con = (HttpURLConnection) url.openConnection();

            //request headers
            con.setConnectTimeout(CONNECTION_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);

            if(headers != null){
                if(headers.size() > 0){
                    Set set = headers.entrySet();// Get a set of the entries
                    Iterator i = set.iterator();// Get an iterator
                    // Display elements
                    while(i.hasNext()) {
                        Map.Entry me = (Map.Entry)i.next();
                        con.addRequestProperty(me.getKey().toString(),me.getValue().toString());
                    }
                }
            }

            switch (type){
                case GET:
                    con.setRequestMethod("GET");// optional default is GET
                    break;
                case POST:
                    con.setRequestMethod("POST"); // Send POST request
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.setFixedLengthStreamingMode(params.length());
                    con.setRequestProperty("Accept-Charset", "UTF-8");
                    con.setRequestProperty("Content-Length",Integer.toString(params.length()));

                    //write data as body
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(params);
                    wr.flush();
                    wr.close();
                    break;
            }

            int responseCode = con.getResponseCode();//receive response code here

            if(responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (IOException e) {
            return null;
        } finally {
            if(con != null)
                con.disconnect(); //this method is for releasing pool
        }
    }

    public String getRawResponse(RequestType type, String URL, String UrlParameters, HashMap<String, String> headers) {
        try {

            String response = "";
            switch (type) {
                case GET:
                    response = RESTRequest(type, URL, UrlParameters, headers);
                    break;
                case POST:
                    response = RESTRequest(type, URL, UrlParameters, headers);
                    break;
            }
            return response;
        } catch (Exception e) {
            return null;
        }
    }

}