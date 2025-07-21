package weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public final class WindForecastService {

  private static final int PAST_DAYS = 3;
  private static final int FORECAST_DAYS = 16;

  private static final ObjectMapper OBJECT_MAPPER;
  static {
    var module = new SimpleModule();
    module.addDeserializer(List.class, new TypeAwareListDeserializer(null));
    var mapper = new ObjectMapper();
    mapper.registerModule(module);
    OBJECT_MAPPER =  mapper;
  }

  static String fetch(URI uri) throws IOException {
    try (var httpClient = HttpClient.newBuilder().build()) {
      var request = HttpRequest.newBuilder().uri(uri).GET().build();
      var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (httpResponse.statusCode() != 200) {
        throw new IOException("API request failed with status code: " + httpResponse.statusCode() +"  " + httpResponse.body());
      }
      return httpResponse.body();
    } catch (InterruptedException e) {
      throw (InterruptedIOException) new InterruptedIOException().initCause(e);
    }
  }

  /**
   * Fetches wind speed forecast for a given location
   *
   * @param latitude Latitude of the location
   * @param longitude Longitude of the location
   * @return List of wind speed forecast records
   * @throws IOException If there's an error with the API call or JSON parsing
   */
  public static List<WindSpeed> getWindSpeedForecast(String latitude, String longitude)
      throws IOException {

    var uri = buildApiUrl(latitude, longitude);
    System.err.println(uri);

    var body = fetch(uri);

    var response = OBJECT_MAPPER.readValue(body, OpenMeteoResponse.class);
    return response.hourly().windSpeed10m();
  }

  private static URI buildApiUrl(String latitude, String longitude) {
    var hourly = "wind_speed_10m";
    var query = "latitude=" + latitude +
        "&longitude=" + longitude +
        "&hourly=" + hourly +
        "&past_days=" + PAST_DAYS +
        "&forecast_days=" + FORECAST_DAYS;
    try {
      return new URI("https", "api.open-meteo.com", "/v1/forecast", query, null);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  public value record WindSpeed(float windSpeed) {
    public WindSpeed(double value) {
      this((float) value);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record HourlyData(@JsonProperty("wind_speed_10m") List<WindSpeed> windSpeed10m) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record OpenMeteoResponse(HourlyData hourly) {}
}
