package com.weatherapp.myweatherapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.repository.VisualcrossingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;
import java.lang.reflect.Field;

@DisplayName("WeatherService Tests")
class WeatherServiceTest {

    @Mock
    private VisualcrossingRepository weatherRepo;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Basic Forecast Tests")
    class BasicForecastTests {
        @Test
        @DisplayName("Should return forecast for valid city")
        void testForecastByCity_Success() {
            CityInfo mockCityInfo = mock(CityInfo.class);
            when(weatherRepo.getByCity("London")).thenReturn(mockCityInfo);

            CityInfo result = weatherService.forecastByCity("London");

            assertNotNull(result);
            verify(weatherRepo).getByCity("London");
        }

        @Test
        @DisplayName("Should handle empty city name")
        void testForecastByCity_WithEmptyCity() {
            when(weatherRepo.getByCity("")).thenReturn(null);

            CityInfo result = weatherService.forecastByCity("");

            assertNull(result);
            verify(weatherRepo).getByCity("");
        }

        @Test
        @DisplayName("Should handle null city name")
        void testForecastByCity_WithNullCity() {
            when(weatherRepo.getByCity(null)).thenReturn(null);

            CityInfo result = weatherService.forecastByCity(null);

            assertNull(result);
            verify(weatherRepo).getByCity(null);
        }

        @Test
        @DisplayName("Should throw exception for invalid city")
        void testForecastByCity_WithInvalidCity() {
            when(weatherRepo.getByCity("InvalidCity"))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

            assertThrows(HttpClientErrorException.class, () -> {
                weatherService.forecastByCity("InvalidCity");
            });
            verify(weatherRepo).getByCity("InvalidCity");
        }
    }

    @Nested
    @DisplayName("Multiple Calls Tests")
    class MultipleCallsTests {
        @Test
        @DisplayName("Should handle multiple calls for same city")
        void testForecastByCity_MultipleCallsSameCity() {
            CityInfo mockCityInfo = mock(CityInfo.class);
            when(weatherRepo.getByCity("London")).thenReturn(mockCityInfo);

            CityInfo result1 = weatherService.forecastByCity("London");
            CityInfo result2 = weatherService.forecastByCity("London");

            assertNotNull(result1);
            assertNotNull(result2);
            verify(weatherRepo, times(2)).getByCity("London");
        }

        @Test
        @DisplayName("Should handle calls for different cities")
        void testForecastByCity_DifferentCities() {
            CityInfo mockCityInfo1 = mock(CityInfo.class);
            CityInfo mockCityInfo2 = mock(CityInfo.class);
            when(weatherRepo.getByCity("London")).thenReturn(mockCityInfo1);
            when(weatherRepo.getByCity("Paris")).thenReturn(mockCityInfo2);

            CityInfo result1 = weatherService.forecastByCity("London");
            CityInfo result2 = weatherService.forecastByCity("Paris");

            assertNotNull(result1);
            assertNotNull(result2);
            verify(weatherRepo).getByCity("London");
            verify(weatherRepo).getByCity("Paris");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        @Test
        @DisplayName("Should handle network errors")
        void testForecastByCity_NetworkError() {
            when(weatherRepo.getByCity("London"))
                    .thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                weatherService.forecastByCity("London");
            });
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
            verify(weatherRepo).getByCity("London");
        }

        @Test
        @DisplayName("Should handle unexpected errors")
        void testForecastByCity_UnexpectedError() {
            when(weatherRepo.getByCity("London"))
                    .thenThrow(new RuntimeException("Unexpected error"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                weatherService.forecastByCity("London");
            });
            assertEquals("Unexpected error", exception.getMessage());
            verify(weatherRepo).getByCity("London");
        }

        @Test
        @DisplayName("Should handle authorization errors")
        void testForecastByCity_AuthorizationError() {
            when(weatherRepo.getByCity("London"))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                weatherService.forecastByCity("London");
            });
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            verify(weatherRepo).getByCity("London");
        }

        @Test
        @DisplayName("Should handle rate limit exceeded")
        void testForecastByCity_RateLimitExceeded() {
            when(weatherRepo.getByCity("London"))
                    .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                weatherService.forecastByCity("London");
            });
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
            verify(weatherRepo).getByCity("London");
        }
    }

    @Nested
    @DisplayName("Special Cases Tests")
    class SpecialCasesTests {
        @Test
        @DisplayName("Should handle city names with special characters")
        void testForecastByCity_SpecialCharacters() {
            CityInfo mockCityInfo = mock(CityInfo.class);
            String cityName = "São Paulo";
            when(weatherRepo.getByCity(cityName)).thenReturn(mockCityInfo);

            CityInfo result = weatherService.forecastByCity(cityName);

            assertNotNull(result);
            verify(weatherRepo).getByCity(cityName);
        }

        @Test
        @DisplayName("Should handle city names with spaces")
        void testForecastByCity_WithSpaces() {
            CityInfo mockCityInfo = mock(CityInfo.class);
            String cityName = "New York";
            when(weatherRepo.getByCity(cityName)).thenReturn(mockCityInfo);

            CityInfo result = weatherService.forecastByCity(cityName);

            assertNotNull(result);
            verify(weatherRepo).getByCity(cityName);
        }

        @Test
        @DisplayName("Should handle very long city names")
        void testForecastByCity_LongCityName() {
            String longCityName = "Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch";
            when(weatherRepo.getByCity(longCityName))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

            assertThrows(HttpClientErrorException.class, () -> {
                weatherService.forecastByCity(longCityName);
            });
            verify(weatherRepo).getByCity(longCityName);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        @Test
        @DisplayName("Should handle consecutive API calls")
        void testForecastByCity_ConsecutiveCalls() {
            CityInfo mockCityInfo = mock(CityInfo.class);
            when(weatherRepo.getByCity(anyString())).thenReturn(mockCityInfo);

            for (int i = 0; i < 5; i++) {
                CityInfo result = weatherService.forecastByCity("London");
                assertNotNull(result);
            }

            verify(weatherRepo, times(5)).getByCity("London");
        }

        @Test
        @DisplayName("Should handle gateway timeout")
        void testForecastByCity_GatewayTimeout() {
            when(weatherRepo.getByCity("London"))
                    .thenThrow(new HttpClientErrorException(HttpStatus.GATEWAY_TIMEOUT));

            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                weatherService.forecastByCity("London");
            });
            assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getStatusCode());
            verify(weatherRepo).getByCity("London");
        }

        @Test
        @DisplayName("Should handle service unavailable with retry")
        void testForecastByCity_ServiceUnavailableWithRetry() {
            when(weatherRepo.getByCity("London"))
                    .thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE))
                    .thenReturn(mock(CityInfo.class));

            assertThrows(HttpClientErrorException.class, () -> {
                weatherService.forecastByCity("London");
            });

            CityInfo result = weatherService.forecastByCity("London");
            assertNotNull(result);

            verify(weatherRepo, times(2)).getByCity("London");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle empty response from API")
        void testForecastByCity_EmptyResponse() {
            when(weatherRepo.getByCity("London")).thenReturn(new CityInfo());

            CityInfo result = weatherService.forecastByCity("London");

            assertNotNull(result);
            verify(weatherRepo).getByCity("London");
        }

        @Test
        @DisplayName("Should handle malformed city names")
        void testForecastByCity_MalformedCityName() {
            String malformedCity = "Lon@don#";
            when(weatherRepo.getByCity(malformedCity))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

            assertThrows(HttpClientErrorException.class, () -> {
                weatherService.forecastByCity(malformedCity);
            });
            verify(weatherRepo).getByCity(malformedCity);
        }

        @Test
        @DisplayName("Should handle case-sensitive city names")
        void testForecastByCity_CaseSensitive() {
            CityInfo mockCityInfo = mock(CityInfo.class);
            when(weatherRepo.getByCity("LONDON")).thenReturn(mockCityInfo);
            when(weatherRepo.getByCity("london")).thenReturn(mockCityInfo);

            CityInfo result1 = weatherService.forecastByCity("LONDON");
            CityInfo result2 = weatherService.forecastByCity("london");

            assertNotNull(result1);
            assertNotNull(result2);
            verify(weatherRepo).getByCity("LONDON");
            verify(weatherRepo).getByCity("london");
        }
    }
}