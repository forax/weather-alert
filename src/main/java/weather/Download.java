package weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class Download {
  private record WindSpeed(@JsonValue double windSpeed) { }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record HourlyData(@JsonProperty("wind_speed_10m") List<WindSpeed> windSpeed) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record OpenMeteoResponse(HourlyData hourly) {}

  static void main() throws IOException {
    // download from open-meteo
    //var uri = URI.create("https://api.open-meteo.com/v1/forecast?latitude=48.864716&longitude=2.349014&hourly=wind_speed_10m&past_days=64&forecast_days=16");

    //var text = WindForecastService.fetch(uri);
    //Files.writeString(Path.of("openmeteo-windspeed.json"), text);

    var text = Files.readString(Path.of("data/openmeteo-windspeed.json"));

    var mapper = new ObjectMapper();
    var response = mapper.readValue(text, OpenMeteoResponse.class);
    var windSpeeds = response.hourly.windSpeed;

    var windSpeedText = mapper.writeValueAsString(windSpeeds);
    Files.writeString(Path.of("data/windspeed.json"), windSpeedText);

    var windSpeedSmallText = mapper.writeValueAsString(windSpeeds.stream().limit(10).toList());
    Files.writeString(Path.of("data/windspeed-small.json"), windSpeedSmallText);
  }
}
