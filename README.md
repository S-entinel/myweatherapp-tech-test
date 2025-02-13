# Weather Application Implementation

## Overview
This Spring Boot application provides weather-related information through REST endpoints, integrating with the Visual Crossing Weather API. The implementation focuses on two main features: comparing daylight hours between cities and checking current rain conditions.

## Features

### 1. Daylight Hours Comparison
Endpoint: `GET /compare-daylight/{city1}/{city2}`

Compares the length of daylight hours between two cities and returns which city has the longest day. Daylight hours are calculated as the time between sunrise and sunset.

Example response:
```
London has the longest day
```

### 2. Rain Check
Endpoint: `GET /check-rain/{city1}/{city2}`

Checks and reports which city (if any) is currently experiencing rain.

Example response:
```
It is raining in London but not in Paris
```

## Technical Implementation

### Architecture
- **Controller Layer**: Handles HTTP requests and response formatting
- **Service Layer**: Contains business logic and API integration
- **Repository Layer**: Manages external API communication
- **Model Layer**: Defines data structures

### Error Handling
- Input validation for city names
- HTTP client error handling
- API error responses
- Data processing error handling

### Testing
Comprehensive test suite covering:
- Basic functionality
- Edge cases
- Error scenarios
- Special character handling
- Network errors
- Multiple API calls

## Setup and Running

### Prerequisites
- Java SDK 17
- Maven 3.6.3+
- Visual Crossing Weather API key

### Configuration
1. Create a free account at [Visual Crossing Weather](https://www.visualcrossing.com/weather-data-editions)
2. Get your API key from the 'My Account' section
3. Add your API key to `src/main/resources/application.properties`:
```properties
weather.visualcrossing.key=YOUR_API_KEY_HERE
```

### Building
```bash
./mvnw clean install
```

### Running
```bash
./mvnw spring-boot:run
```

### Running Tests
```bash
./mvnw test
```

## API Usage

### Comparing Daylight Hours
```bash
curl http://localhost:8080/compare-daylight/London/Tokyo
```

### Checking Rain
```bash
curl http://localhost:8080/check-rain/London/Paris
```

## Design Decisions

### Reflection Usage
- Implemented reflection to access model fields without modifying existing classes
- Added robust error handling for reflection operations
- Maintained encapsulation while accessing necessary data

### Response Format
- Simple, clear responses focusing on required information
- Consistent error message format
- Appropriate HTTP status codes for different scenarios

### Testing Strategy
- Unit tests for all major components
- Integration tests for API interaction
- Edge case testing for robust operation
- Performance testing for multiple calls

## Error Handling

### HTTP Status Codes
- 200: Successful operation
- 400: Invalid input (empty or malformed city names)
- 404: City not found
- 500: Internal server error
- 503: External API unavailable

### Error Response Format
```json
{
    "message": "Error description"
}
```

## Limitations and Future Improvements

### Current Limitations
- Reflection-based access to model fields
- No caching implementation
- Synchronous API calls

### Potential Improvements
- Add caching layer for frequently requested cities
- Implement asynchronous processing
- Add metric collection
- Implement rate limiting
- Add API versioning

## Security Considerations
- API key storage in properties file
- Input validation and sanitization
- Error message security
- Rate limiting (potential future addition)

## Testing
To run the test suite:
```bash
./mvnw test
```
