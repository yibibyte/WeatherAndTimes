package ru.japp.weathertime.weathertime;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class WeatherTimeApp extends Application {

    // Удаляем статические константы
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private ComboBox<String> locationComboBox;
    private TextArea weatherArea;
    private TextArea timeArea;
    private final Map<String, String> timezoneMap = new HashMap<>();

    @Override
    public void start(Stage stage) {
        initializeTimezoneMap();

        // GUI Components
        locationComboBox = new ComboBox<>();
        locationComboBox.getItems().addAll(
                "London", "Paris", "New-York", "Tokyo",
                "Moscow", "Berlin", "Dubai", "Sydney"
        );

        locationComboBox.setPromptText("Укажите местоположение");

        Button weatherButton = new Button("Получить погоду");
        Button timeButton = new Button("Получить время");

        weatherArea = new TextArea();
        weatherArea.setEditable(false);
        weatherArea.setWrapText(true);

        timeArea = new TextArea();
        timeArea.setEditable(false);
        timeArea.setWrapText(true);

        // Layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(
                locationComboBox,
                new Separator(),
                weatherButton,
                weatherArea,
                new Separator(),
                timeButton,
                timeArea
        );

        // Event Handlers
        weatherButton.setOnAction(e -> fetchWeather());
        timeButton.setOnAction(e -> fetchTime());

        // Scene
        Scene scene = new Scene(root, 450, 450);
        stage.setTitle("Погода & Время App");
        stage.setScene(scene);
        stage.show();
    }

    private void initializeTimezoneMap() {
        timezoneMap.put("London", "Europe/London");
        timezoneMap.put("Paris", "Europe/Paris");
        timezoneMap.put("New-York", "America/New_York");
        timezoneMap.put("Tokyo", "Asia/Tokyo");
        timezoneMap.put("Moscow", "Europe/Moscow");
        timezoneMap.put("Berlin", "Europe/Berlin");
        timezoneMap.put("Dubai", "Asia/Dubai");
        timezoneMap.put("Sydney", "Australia/Sydney");
    }

    private void fetchWeather() {
        String location = locationComboBox.getValue();
        if (location == null || location.isEmpty()) return;

        String url = "http://api.weatherapi.com/v1/current.json" +
                "?key=" + Config.getWeatherApiKey() +  // Используем Config
                "&q=" + location +
                "&aqi=no";

        client.sendAsync(
                        HttpRequest.newBuilder().uri(URI.create(url)).build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::parseWeatherResponse)
                .exceptionally(e -> {
                    updateWeatherArea("Ошибка: " + e.getMessage());
                    return null;
                });
    }

    private void parseWeatherResponse(String response) {
        try {
            JsonNode root = mapper.readTree(response);
            JsonNode current = root.get("current");

            StringBuilder sb = new StringBuilder();
            sb.append("Temperature (Температура): ").append(current.get("temp_c").asDouble()).append("°C\n");
            sb.append("Condition (Погода): ").append(current.get("condition").get("text").asText()).append("\n");
            sb.append("Humidity (Влажность): ").append(current.get("humidity").asInt()).append("%\n");
            sb.append("Wind (Ветер): ").append(current.get("wind_kph").asDouble()).append(" km/h\n");
            sb.append("Pressure (Атмосферное давление): ").append(current.get("pressure_mb").asDouble()).append(" mb\n");
            sb.append("Feels like (По ощущениям): ").append(current.get("feelslike_c").asDouble()).append("°C");

            updateWeatherArea(sb.toString());
        } catch (Exception e) {
            updateWeatherArea("Ошибка при получении данных о погоде.");
        }
    }

    private void fetchTime() {
        String location = locationComboBox.getValue();
        if (location == null || location.isEmpty()) return;

        String zoneId = timezoneMap.get(location);
        if (zoneId == null) return;

        String url = "http://api.timezonedb.com/v2.1/get-time-zone" +
                "?key=" + Config.getTimezoneApiKey() +  // Используем Config
                "&format=json" +
                "&by=zone" +
                "&zone=" + zoneId;

        client.sendAsync(
                        HttpRequest.newBuilder().uri(URI.create(url)).build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::parseTimeResponse)
                .exceptionally(e -> {
                    updateTimeArea("Ошибка: " + e.getMessage());
                    return null;
                });
    }

    private void parseTimeResponse(String response) {
        try {
            JsonNode root = mapper.readTree(response);
            long timestamp = root.get("timestamp").asLong();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

            String formattedTime = formatter.format(Instant.ofEpochSecond(timestamp));
            updateTimeArea("Место время в городе:\n" + formattedTime);
        } catch (Exception e) {
            updateTimeArea("Ошибка при получении данных о времени.");
        }
    }

    private void updateWeatherArea(String text) {
        Platform.runLater(() -> weatherArea.setText(text));
    }

    private void updateTimeArea(String text) {
        Platform.runLater(() -> timeArea.setText(text));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
