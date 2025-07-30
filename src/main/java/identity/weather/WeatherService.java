package identity.weather;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

public final class WeatherService {

  private static final ObjectReader OBJECT_READER = new ObjectMapper().reader();

  static String fetch(URI uri) throws IOException {
    try (var httpClient = HttpClient.newBuilder().build()) {
      var request = HttpRequest.newBuilder().uri(uri).GET().build();
      var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (httpResponse.statusCode() != 200) {
        throw new IOException("API request failed with status code: " + httpResponse.statusCode() + "  " + httpResponse.body());
      }
      return httpResponse.body();
    } catch (InterruptedException e) {
      throw (InterruptedIOException) new InterruptedIOException().initCause(e);
    }
  }

  private static final Path CACHE_DIR = Paths.get("cache");
  static {
    try {
      Files.createDirectories(CACHE_DIR);
    } catch (IOException e) {
      // do nothing, the cache is just disable
    }
  }

  private static Path cachePath(URI uri) {
    return CACHE_DIR.resolve(uri.getQuery());
  }

  private static String readFromCache(URI uri) throws IOException {
    return Files.readString(cachePath(uri));
  }

  private static void storeIntoCache(URI uri, String json) throws IOException {
    Files.writeString(cachePath(uri), json);
  }

  public static List<WeatherData> getWeatherData(LatLong latLong, LocalDate startDate, LocalDate endDate)
      throws IOException {

    var uri = buildURI(latLong, startDate, endDate);
    System.err.println(uri);

    String body;
    try {
      body = readFromCache(uri);
    } catch (IOException e) {
      body = fetch(uri);
      storeIntoCache(uri, body);
    }

    var response = OBJECT_READER.readValue(body, OpenMeteoResponse.class);
    var data = response.hourly();
    if (data.temperatures.size() != data.windspeeds.size() || data.temperatures.size() != data.precipitations.size()) {
      throw new IllegalStateException("temperature size != windspeed size or precipitation size != precipitation size");
    }
    var start = System.nanoTime();
    // use util.AggregateList instead ?
    var weatherData = IntStream.range(0, data.precipitations.size())
        .mapToObj(i -> new WeatherData(
            data.temperatures.get(i),
            data.windspeeds.get(i),
            data.precipitations.get(i)))
        .toList();
    var end = System.nanoTime();
    System.out.println("Parsing time: " + (end - start) + " ns");
    return weatherData;
  }

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private static URI buildURI(LatLong latLong, LocalDate startDate, LocalDate endDate) {
    var names = "temperature_2m,wind_speed_10m,precipitation";
    var query = "latitude=" + latLong.latitude +
        "&longitude=" + latLong.longitude +
        "&start_date=" + DATE_FORMATTER.format(startDate) +
        "&end_date=" + DATE_FORMATTER.format(endDate) +
        "&hourly=" + names;
    try {
      return new URI("https", "archive-api.open-meteo.com", "/v1/archive", query, null);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  public record LatLong(double latitude, double longitude) {}

  public record WeatherData(Temperature temperature, Windspeed windspeed, Precipitation precipitation) { }

  public record Temperature(float value) {
    @JsonCreator
    public Temperature(double value) {
      this((float) value);
    }

    @Override
    public String toString() {
      return value + " °C";
    }

    public Temperature min(Temperature temperature) {
      return new Temperature(Math.min(value, temperature.value));
    }

    public Temperature max(Temperature temperature) {
      return new Temperature(Math.max(value, temperature.value));
    }
  }

  public record Windspeed(float value) {
    public Windspeed {
      if (value < 0) {
        throw new IllegalArgumentException("value < 0");
      }
      //super();
    }

    @JsonCreator
    public Windspeed(double value) {
      this((float) value);
    }

    @Override
    public String toString() {
      return value + " km/h";
    }

    public Windspeed max(Windspeed windspeed) {
      return new Windspeed(Math.max(value, windspeed.value));
    }
  }

  public record Precipitation(float value) {
    public Precipitation {
      if (value < 0) {
        throw new IllegalArgumentException("value < 0");
      }
      //super();
    }

    @JsonCreator
    public Precipitation(double value) {
      this((float) value);
    }

    @Override
    public String toString() {
      return value + " mm";
    }

    public Precipitation add(Precipitation precipitation) {
      return new Precipitation(value + precipitation.value);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record HourlyData(
      @JsonProperty("temperature_2m") List<Temperature> temperatures,
      @JsonProperty("wind_speed_10m") List<Windspeed> windspeeds,
      @JsonProperty("precipitation") List<Precipitation> precipitations
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record OpenMeteoResponse(HourlyData hourly) {}
}
