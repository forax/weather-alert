package primitive.weather;

import java.io.IOException;
import java.time.LocalDate;

class WeatherExample {
  static void main() throws IOException {
    // Coordinates
    var paris = new WeatherService.LatLong(48.864716, 2.349014);
    var noirmoutier = new WeatherService.LatLong(46.966667, -2.216667);
    var toulon = new WeatherService.LatLong(43.125833, 5.930556);

    // Ten years, 2025-2015
    var endDate = LocalDate.parse("2025-01-01");
    var startDate = endDate.minusYears(10);

    var weatherData = WeatherService.getWeatherData(paris, startDate, endDate);
    System.out.println(WeatherComputation.compute(weatherData));
  }
}
