package value.weather;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
import util.TypeAwareListDeserializer;

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
import java.util.Objects;

import static util.Fetch.*;

public final class WeatherService {

  private static final ObjectReader OBJECT_READER;
  static {
    var module = new SimpleModule();
    module.addDeserializer(List.class, new TypeAwareListDeserializer(null));
    var mapper = new ObjectMapper();
    mapper.registerModule(module);
    OBJECT_READER =  mapper.reader();
  }

  public static HourlyData getHourlyData(LatLong latLong, LocalDate startDate, LocalDate endDate)
      throws IOException{

    var uri = new QueryBuilder(latLong, startDate, endDate).toURI();
    System.err.println(uri);

    String body;
    try {
      body = readFromCache(uri);
    } catch (IOException e) {
      body = fetch(uri);
      storeIntoCache(uri, body);
    }

    var response = OBJECT_READER.readValue(body, OpenMeteoResponse.class);
    return response.hourly();
  }

  public value record LatLong(double latitude, double longitude) {}

  public value record Temperature(float value) {
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

  public value record Windspeed(float value) {
    public Windspeed {
      if (value < 0) {
        throw new IllegalArgumentException("value < 0");
      }
      // super();  // comment for IntelliJ
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

  public value record Precipitation(float value) {
    public Precipitation {
      if (value < 0) {
        throw new IllegalArgumentException("value < 0");
      }
      // super();   // comment for IntelliJ
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
  public record HourlyData(
      @JsonProperty("temperature_2m") List<Temperature> temperatures,
      @JsonProperty("wind_speed_10m") List<Windspeed> windspeeds,
      @JsonProperty("precipitation") List<Precipitation> precipitations
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record OpenMeteoResponse(HourlyData hourly) {}
}
