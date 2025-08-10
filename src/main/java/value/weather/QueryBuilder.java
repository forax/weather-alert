package value.weather;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import value.weather.WeatherService.LatLong;

public value record QueryBuilder(LatLong latLong, LocalDate startDate, LocalDate endDate) {
  public QueryBuilder {
    Objects.requireNonNull(latLong, "LatLong is required");
    Objects.requireNonNull(startDate, "StartDate is required");
    Objects.requireNonNull(endDate, "EndDate is required");
    if (!startDate.isBefore(endDate)) {
      throw new IllegalArgumentException("StartDate must be before endDate");
    }
    // super();
  }

  public QueryBuilder(LatLong latLong) {
    var now = LocalDate.now();
    this(latLong, now.minusDays(1), now);
  }

  public QueryBuilder dateRange(LocalDate startDate, LocalDate endDate) {
    return new QueryBuilder(latLong, startDate, endDate);
  }

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public URI toURI() {
    var names = "temperature_2m,wind_speed_10m,precipitation";
    var query =
        "latitude=" + latLong.latitude() +
        "&longitude=" + latLong.longitude() +
        "&start_date=" + DATE_FORMATTER.format(startDate) +
        "&end_date=" + DATE_FORMATTER.format(endDate) +
        "&hourly=" + names;
    try {
      return new URI("https", "archive-api.open-meteo.com", "/v1/archive", query, null);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }
}
