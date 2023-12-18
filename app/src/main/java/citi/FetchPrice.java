package citi;

import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.Timestamp;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class FetchPrice {

    private String result;

    public String getResult() {
        return result;
    }

    public void fetch() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String ts = timestamp.toString();
        try {
            URL url = new URL("https://www.google.com/finance/quote/.DJI:INDEXDJX");
            
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

                String price = document.select(".kf1m0").text();
                result = ts + " price:" + price;
            } else {
                result = ts + "price: None";
                System.out.println("Failed to fetch data. Response Code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}