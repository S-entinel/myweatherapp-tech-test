package com.weatherapp.myweatherapp.controller;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import java.lang.reflect.Field;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller handling weather-related endpoints for the Weather Application.
 * Provides functionality for retrieving weather forecasts, comparing daylight hours,
 * and checking rain conditions between cities.
 */
@Controller
public class WeatherController {

  @Autowired
  private WeatherService weatherService;

  /** Set of terms that indicate rain conditions in weather descriptions */
  private static final Set<String> RAIN_CONDITIONS = new HashSet<>(Arrays.asList(
          "rain", "drizzle", "shower", "thunderstorm", "precipitation",
          "downpour", "rainfall", "raining", "stormy"
  ));

  /** Formatter for parsing time strings from the weather API */
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  /**
   * Retrieves the weather forecast for a specified city.
   *
   * @param city The name of the city to get the forecast for
   * @return ResponseEntity containing the CityInfo if successful, or appropriate error response
   */
  @GetMapping("/forecast/{city}")
  public ResponseEntity<CityInfo> forecastByCity(@PathVariable("city") String city) {
    try {
      if (city == null || city.trim().isEmpty()) {
        return ResponseEntity.badRequest().build();
      }
      CityInfo ci = weatherService.forecastByCity(city);
      return ResponseEntity.ok(ci);
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Compares the daylight hours between two cities and returns which city has the longest day.
   * Daylight hours are calculated as the time between sunrise and sunset.
   *
   * @param city1 The name of the first city
   * @param city2 The name of the second city
   * @return ResponseEntity containing a simple statement of which city has the longest day
   */
  @GetMapping("/compare-daylight/{city1}/{city2}")
  public ResponseEntity<String> compareDaylightHours(
          @PathVariable("city1") String city1,
          @PathVariable("city2") String city2) {

    if (city1 == null || city2 == null ||
            city1.trim().isEmpty() || city2.trim().isEmpty()) {
      return ResponseEntity.badRequest()
              .body("City names cannot be empty");
    }

    try {
      CityInfo city1Info = weatherService.forecastByCity(city1);
      CityInfo city2Info = weatherService.forecastByCity(city2);

      if (city1Info == null || city2Info == null) {
        return ResponseEntity.notFound()
                .build();
      }

      DaylightInfo daylight1 = extractDaylightInfo(city1Info, city1);
      DaylightInfo daylight2 = extractDaylightInfo(city2Info, city2);

      long minutes1 = daylight1.getDaylightMinutes();
      long minutes2 = daylight2.getDaylightMinutes();

      return formatDaylightResponse(city1, city2, minutes1, minutes2);

    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode())
              .body("Error accessing weather data: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
              .body("An unexpected error occurred: " + e.getMessage());
    }
  }

  /**
   * Checks and compares the current rain conditions between two cities.
   * Returns a simple statement of which city(ies) it is raining in.
   *
   * @param city1 The name of the first city
   * @param city2 The name of the second city
   * @return ResponseEntity containing a statement of where it is raining
   */
  @GetMapping("/check-rain/{city1}/{city2}")
  public ResponseEntity<String> checkRain(
          @PathVariable("city1") String city1,
          @PathVariable("city2") String city2) {

    if (city1 == null || city2 == null ||
            city1.trim().isEmpty() || city2.trim().isEmpty()) {
      return ResponseEntity.badRequest()
              .body("City names cannot be empty");
    }

    try {
      CityInfo city1Info = weatherService.forecastByCity(city1);
      CityInfo city2Info = weatherService.forecastByCity(city2);

      if (city1Info == null || city2Info == null) {
        return ResponseEntity.notFound()
                .build();
      }

      String conditions1 = getWeatherConditions(city1Info);
      String conditions2 = getWeatherConditions(city2Info);

      boolean isRaining1 = isRaining(conditions1);
      boolean isRaining2 = isRaining(conditions2);

      if (isRaining1 && isRaining2) {
        return ResponseEntity.ok("It is raining in both " + city1 + " and " + city2);
      } else if (isRaining1) {
        return ResponseEntity.ok("It is raining in " + city1);
      } else if (isRaining2) {
        return ResponseEntity.ok("It is raining in " + city2);
      } else {
        return ResponseEntity.ok("It is not raining in either city");
      }

    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode())
              .body("Error accessing weather data: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
              .body("An unexpected error occurred: " + e.getMessage());
    }
  }

  /**
   * Extracts sunrise and sunset times from the CityInfo object using reflection.
   *
   * @param cityInfo The CityInfo object containing weather data
   * @param cityName The name of the city (used for error messages)
   * @return DaylightInfo object containing parsed sunrise and sunset times
   * @throws Exception if data cannot be extracted or is invalid
   */
  private DaylightInfo extractDaylightInfo(CityInfo cityInfo, String cityName) throws Exception {
    Field conditionsField = cityInfo.getClass().getDeclaredField("currentConditions");
    conditionsField.setAccessible(true);
    Object conditions = conditionsField.get(cityInfo);

    if (conditions == null) {
      throw new RuntimeException("No weather conditions available for " + cityName);
    }

    Field sunriseField = conditions.getClass().getDeclaredField("sunrise");
    Field sunsetField = conditions.getClass().getDeclaredField("sunset");
    sunriseField.setAccessible(true);
    sunsetField.setAccessible(true);

    String sunrise = (String) sunriseField.get(conditions);
    String sunset = (String) sunsetField.get(conditions);

    if (sunrise == null || sunset == null) {
      throw new RuntimeException("Missing sunrise/sunset data for " + cityName);
    }

    return new DaylightInfo(sunrise, sunset);
  }

  /**
   * Extracts current weather conditions from the CityInfo object using reflection.
   *
   * @param cityInfo The CityInfo object containing weather data
   * @return String describing current weather conditions, or "Unknown" if not available
   * @throws Exception if data cannot be extracted
   */
  private String getWeatherConditions(CityInfo cityInfo) throws Exception {
    Field conditionsField = cityInfo.getClass().getDeclaredField("currentConditions");
    conditionsField.setAccessible(true);
    Object conditions = conditionsField.get(cityInfo);

    if (conditions == null) {
      return "Unknown";
    }

    Field weatherConditionsField = conditions.getClass().getDeclaredField("conditions");
    weatherConditionsField.setAccessible(true);
    String weatherConditions = (String) weatherConditionsField.get(conditions);

    return weatherConditions != null ? weatherConditions : "Unknown";
  }

  /**
   * Formats the daylight comparison response according to requirements.
   *
   * @param city1 Name of the first city
   * @param city2 Name of the second city
   * @param minutes1 Daylight minutes for the first city
   * @param minutes2 Daylight minutes for the second city
   * @return ResponseEntity containing formatted comparison
   */
  private ResponseEntity<String> formatDaylightResponse(String city1, String city2,
                                                        long minutes1, long minutes2) {
    if (minutes1 > minutes2) {
      return ResponseEntity.ok(city1 + " has the longest day");
    } else if (minutes2 > minutes1) {
      return ResponseEntity.ok(city2 + " has the longest day");
    } else {
      return ResponseEntity.ok("Both cities have equal daylight hours");
    }
  }

  /**
   * Checks if the given weather conditions indicate rain.
   *
   * @param conditions The weather conditions string to check
   * @return true if the conditions indicate rain, false otherwise
   */
  private boolean isRaining(String conditions) {
    if (conditions == null) return false;
    String lowerConditions = conditions.toLowerCase();
    return RAIN_CONDITIONS.stream()
            .anyMatch(lowerConditions::contains);
  }

  /**
   * Inner class for handling daylight information and calculations.
   * Stores sunrise and sunset times and provides methods for calculating daylight duration.
   */
  private static class DaylightInfo {
    private final LocalTime sunrise;
    private final LocalTime sunset;

    /**
     * Creates a new DaylightInfo instance from sunrise and sunset time strings.
     *
     * @param sunrise The sunrise time in HH:mm:ss format
     * @param sunset The sunset time in HH:mm:ss format
     */
    DaylightInfo(String sunrise, String sunset) {
      this.sunrise = LocalTime.parse(sunrise, TIME_FORMATTER);
      this.sunset = LocalTime.parse(sunset, TIME_FORMATTER);
    }

    /**
     * Calculates the number of minutes of daylight.
     * Handles cases where sunset occurs on the next day.
     *
     * @return The number of minutes between sunrise and sunset
     */
    long getDaylightMinutes() {
      long minutes = ChronoUnit.MINUTES.between(sunrise, sunset);
      // Handle case where sunset is on the next day
      if (minutes < 0) {
        minutes += 24 * 60;
      }
      return minutes;
    }
  }
}