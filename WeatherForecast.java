import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherForecast {

    public static void main(String[] args) {
        try {
            // Bloomington data
            String latitude = "39.1658";
            String longitude = "-86.5267";
            String hourly = "temperature_2m";
            String tempUnit = "fahrenheit";
            String timezone = "EST";

            String urlString = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=%s&temperature_unit=%s&timezone=%s",
                    latitude, longitude, hourly, tempUnit, timezone);

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HTTP GET Request Failed " + responseCode);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonObject hourlyData = jsonResponse.getAsJsonObject("hourly");
            JsonArray times = hourlyData.getAsJsonArray("time");
            JsonArray temperatures = hourlyData.getAsJsonArray("temperature_2m");
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            DateTimeFormatter outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            System.out.println("Bloomington 7-Day Forecast in Fahrenheit:");
            LocalDateTime nextTime = currentDateTime.truncatedTo(ChronoUnit.HOURS).plusHours(3);
            LocalDateTime currentDate = null;

            for (int i = 0; i < times.size(); i++) {
                String time = times.get(i).getAsString();
                LocalDateTime forecastTime = LocalDateTime.parse(time, inputFormatter);

                if (forecastTime.isBefore(nextTime)) {
                    continue;
                }

                nextTime = nextTime.plusHours(3);

                if (currentDate == null || !forecastTime.toLocalDate().equals(currentDate.toLocalDate())) {
                    currentDate = forecastTime;
                    System.out.println("\nForecast for " + currentDate.format(outputDateFormatter) + ":");
                }

                String timeFormatted = forecastTime.format(outputTimeFormatter);
                double temperature = temperatures.get(i).getAsDouble();
                System.out.printf("%s: %.1f°F\n", timeFormatted, temperature);

                if (forecastTime.isAfter(currentDateTime.plusDays(7))) {
                    break;}}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//sources: https://www.baeldung.com/java-org-json, https://www.tutorialspoint.com/gson/gson_quick_guide.htm, https://docs.oracle.com/javaee/7/api/javax/json/JsonObject.html

