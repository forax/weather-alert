package flightrecord;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Random;

public final class JfrRecordArray {

  private static final Random RANDOM = new Random();

  void main() throws IOException, ParseException {

    int iterations = 100_000_000;

    Configuration configuration = Configuration.getConfiguration("profile");
    try (Recording recording = new Recording(configuration)) {
      recording.setName("Identities");
      recording.start();

      var values = new identity.weather.WeatherComputation.WeatherData[iterations];
      for (int i = 0; i < iterations; i++) {
        var temperature = new identity.weather.WeatherService.Temperature(RANDOM.nextFloat());
        var windspeed = new identity.weather.WeatherService.Windspeed(RANDOM.nextFloat());
        var precipitation = new identity.weather.WeatherService.Precipitation(RANDOM.nextFloat());

        var weatherData = new identity.weather.WeatherComputation.WeatherData(temperature, windspeed, precipitation);
        values[i] = weatherData;
      }
      var result = identity.weather.WeatherComputation.computeWeatherData(values);
      System.out.println(result);

      recording.stop();
      recording.dump(Path.of("test-identity-array.jfr"));
    }

    try (Recording recording = new Recording(configuration)) {
      recording.setName("Values");
      recording.start();

      var values = new value.weather.WeatherComputation.WeatherData[iterations];
      for (int i = 0; i < iterations; i++) {
        var temperature = new value.weather.WeatherService.Temperature(RANDOM.nextFloat());
        var windspeed = new value.weather.WeatherService.Windspeed(RANDOM.nextFloat());
        var precipitation = new value.weather.WeatherService.Precipitation(RANDOM.nextFloat());

        var weatherData = new value.weather.WeatherComputation.WeatherData(temperature, windspeed, precipitation);
        values[i] = weatherData;
      }
      var result = value.weather.WeatherComputation.computeWeatherData(values);
      System.out.println(result);

      recording.stop();
      recording.dump(Path.of("test-value-array.jfr"));
    }
  }
}
