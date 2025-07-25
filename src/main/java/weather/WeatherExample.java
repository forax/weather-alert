package weather;

import java.io.IOException;
import java.time.LocalDate;

// --enable-preview --add-exports=java.base/jdk.internal.value=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
class WeatherExample {
  static void main() throws IOException {
    // Coordinates
    var paris = new WeatherService.LatLong(48.864716, 2.349014);
    var noirmoutier = new WeatherService.LatLong(46.966667, -2.216667);
    var toulon = new WeatherService.LatLong(43.125833, 5.930556);

    // One year, 2024-2025
    var endDate = LocalDate.parse("2025-01-01");
    var startDate = endDate.minusYears(1);

    var weatherData = WeatherService.getWeatherData(paris, startDate, endDate);
    System.out.println(WeatherComputation.compute(weatherData));
  }
}
