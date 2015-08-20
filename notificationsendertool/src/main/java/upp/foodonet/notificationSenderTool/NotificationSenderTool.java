package upp.foodonet.notificationSenderTool;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;

public class NotificationSenderTool {

    static final String GoogleServerUri = "https://gcm-http.googleapis.com/gcm/send";
    static final String SERVER_API_KEY = "AIzaSyBG-UhoLKYq5O-wJPw_hLF6t3tyf7vIGjQ";
    static final String Sender_ID = "776321412578";
    static final String USER_AGENT = "";

    public static void main(String[] args)
            throws Exception
    {
        NotificationSenderTool instance = new NotificationSenderTool();
        instance.run();
    }

    private NotificationSenderTool()
    {
    }

    private void run() throws Exception
    {
        HttpsURLConnection conn = openAndConfigureConnection();

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postBody);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }

    private HttpsURLConnection openAndConfigureConnection() throws Exception
    {
        url = new URL(GoogleServerUri);
        HttpsURLConnection conn;
        try {
            conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "key=" + SERVER_API_KEY);
            return conn;
        } catch (IOException e) {
            System.out.println("error opening/configuring connection");
            e.printStackTrace();
            throw  e;
        }
    }

    private URL url;
    private String postBody;
}
