package citi;

import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.Timestamp;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class FetchTicker {

    private String ticker = "None";

    public String getResult() {
        return ticker;
    }

    public void fetch(String stockname) {

        try {
            String[] words = stockname.split(" ");
            URL url= new URL("https://www.google.com/search?q=stock+ticker+symbol+" + String.join("+", words));
            System.out.println(url);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

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

                String raw = document.select("div.iAIpCb[data-attrid=\"subtitle\"]").text();
                String[] ls = raw.split(":");
                ticker = ls[1].trim() + ":" + ls[0].trim();
                System.out.println(ticker);
            } else {
                System.out.println("Failed to fetch ticker. Response Code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
        
        
        