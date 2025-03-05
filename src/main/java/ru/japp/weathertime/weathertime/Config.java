package ru.japp.weathertime.weathertime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Не удается найти файл конфигурации");
            }
            props.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Ошибка при загрузке файла конфигурации", ex);
        }
    }

    public static String getWeatherApiKey() {
        return props.getProperty("WEATHER_API_KEY");
    }

    public static String getTimezoneApiKey() {
        return props.getProperty("TIMEZONE_API_KEY" );
    }
}