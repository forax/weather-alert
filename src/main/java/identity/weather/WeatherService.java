package identity.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
import util.Fetch;
import util.FloatConstructorDeserializerModifier;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public final class WeatherService {

  private static final ObjectReader OBJECT_READER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .registerModule(new SimpleModule().setDeserializerModifier(new FloatConstructorDeserializerModifier()))
      .reader();

  public static HourlyData getHourlyData(LatLong latLong, LocalDate startDate, LocalDate endDate)
      throws IOException {

    var uri = new QueryBuilder(latLong).dateRange(startDate, endDate).toURI();

    var body = Fetch.cache(uri, Fetch::fetch);

    var response = OBJECT_READER.readValue(body, OpenMeteoResponse.class);
    return response.hourly();
  }

  public record LatLong(double latitude, double longitude) {}

  public record Temperature(float value) {
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
    }

    @Override
    public String toString() {
      return value + " mm";
    }

    public Precipitation add(Precipitation precipitation) {
      return new Precipitation(value + precipitation.value);
    }
  }

  public record HourlyData(
      @JsonProperty("temperature_2m") List<Temperature> temperatures,
      @JsonProperty("wind_speed_10m") List<Windspeed> windspeeds,
      @JsonProperty("precipitation") List<Precipitation> precipitations
  ) {}

  private record OpenMeteoResponse(HourlyData hourly) {}
}
