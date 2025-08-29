package value.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.LogicalType;
import util.Fetch;
import util.TypeAwareListDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public final class WeatherService {

  private static final ObjectReader OBJECT_READER;
  static {
    var mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.coercionConfigFor(LogicalType.Float)
        .setCoercion(CoercionInputShape.Float, CoercionAction.TryConvert);
    var module = new SimpleModule();
    module.addDeserializer(List.class, new TypeAwareListDeserializer(null));
    mapper.registerModule(module);
    OBJECT_READER = mapper.reader();
  }

  public static HourlyData getHourlyData(LatLong latLong, LocalDate startDate, LocalDate endDate)
      throws IOException {

    var uri = new QueryBuilder(latLong).dateRange(startDate, endDate).toURI();

    var body = Fetch.cache(uri, Fetch::fetch);

    var response = OBJECT_READER.readValue(body, OpenMeteoResponse.class);
    return response.hourly();
  }

  public value record LatLong(double latitude, double longitude) {}

  public value record Temperature(float value) {
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

  public record HourlyData(
      @JsonProperty("temperature_2m") List<Temperature> temperatures,
      @JsonProperty("wind_speed_10m") List<Windspeed> windspeeds,
      @JsonProperty("precipitation") List<Precipitation> precipitations
  ) {}

  private record OpenMeteoResponse(HourlyData hourly) {}
}
