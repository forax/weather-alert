package primitive.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.LogicalType;
import util.Fetch;

import java.io.IOException;
import java.time.LocalDate;

public final class WeatherService {

  private static final ObjectReader OBJECT_READER;
  static {
    var mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.coercionConfigFor(LogicalType.Float)
        .setCoercion(CoercionInputShape.Float, CoercionAction.TryConvert);
    OBJECT_READER = mapper.reader();
  }

  public static HourlyData getHourlyData(LatLong latLong, LocalDate startDate, LocalDate endDate)
      throws IOException {

    var uri = new QueryBuilder(latLong).dateRange(startDate, endDate).toURI();

    var body = Fetch.cache(uri, Fetch::fetch);

    var response = OBJECT_READER.readValue(body, OpenMeteoResponse.class);
    return response.hourly();
  }

  public record LatLong(double latitude, double longitude) {}

  public record HourlyData(
      @JsonProperty("temperature_2m") float[] temperatures,
      @JsonProperty("wind_speed_10m") float[] windspeeds,
      @JsonProperty("precipitation") float[] precipitations
  ) {}

  private record OpenMeteoResponse(HourlyData hourly) {}
}
