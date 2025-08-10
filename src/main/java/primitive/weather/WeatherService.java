package primitive.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import util.Fetch;

import java.io.IOException;
import java.time.LocalDate;

public final class WeatherService {

  private static final ObjectReader OBJECT_READER = new ObjectMapper().reader();

  public static HourlyData getHourlyData(LatLong latLong, LocalDate startDate, LocalDate endDate)
      throws IOException {

    var uri = new QueryBuilder(latLong, startDate, endDate).toURI();
    //System.err.println(uri);

    var body = Fetch.cache(uri, Fetch::fetch);

    var response = OBJECT_READER.readValue(body, OpenMeteoResponse.class);
    return response.hourly();
  }

  public record LatLong(double latitude, double longitude) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record HourlyData(
      @JsonProperty("temperature_2m") float[] temperatures,
      @JsonProperty("wind_speed_10m") float[] windspeeds,
      @JsonProperty("precipitation") float[] precipitations
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record OpenMeteoResponse(HourlyData hourly) {}
}
