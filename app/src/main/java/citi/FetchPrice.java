package citi;

import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.Timestamp;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class FetchPrice {

    private String[] result; // timestamp, price, unit

    public String[] getResult() {
        return result;
    }

    public void fetch(String ticker) {
        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String ts = timestamp.toString();

        try {
            URL url = new URL("https://www.google.com/finance/quote/" + ticker);
            System.out.println("Fetching Price:" + url);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                String html = response.toString();
                Document document = Jsoup.parse(html);

                String raw = document.select(".kf1m0").first().text();
                String unit = "";
                String price = raw;
                if (raw.charAt(0) == '$') {
                    unit = "USD";
                    price = raw.substring(1);
                }
                result = new String[] {ts, price, unit};
                System.out.println(ts + ": " + price + unit);
            } else {
                result = new String[] {ts, "", ""};
                System.out.println("Failed to fetch data. Response Code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}