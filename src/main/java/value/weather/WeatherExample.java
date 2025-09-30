package value.weather;

import java.io.IOException;
import java.time.LocalDate;

// --enable-preview --add-exports=java.base/jdk.internal.value=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
// -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlineLayout
// -XX:+UseCompactObjectHeaders
class WeatherExample {
  static void main() throws IOException {
    // Coordinates
    var paris = new WeatherService.LatLong(48.864716, 2.349014);
    var noirmoutier = new WeatherService.LatLong(46.966667, -2.216667);
    var toulon = new WeatherService.LatLong(43.125833, 5.930556);

    // Twenty years, 2025-2005
    var endDate = LocalDate.parse("2025-01-01");
    var startDate = endDate.minusYears(20);
    var hourlyData = WeatherService.getHourlyData(paris, startDate, endDate);

    // 1
    var weatherData = WeatherComputation.toWeatherData(hourlyData);
    IO.println(WeatherComputation.computeWeatherData(weatherData));

    // 2
    IO.println(WeatherComputation.computeHourlyData(hourlyData));
  }
}
