package weather;

import java.io.IOException;
import java.time.LocalDate;

class WeatherExample {
  public static void main(String[] args) throws IOException {
    // Paris coordinates
    var paris = new WeatherService.LatLong(48.864716, 2.349014);

    // One year, 2024-2025
    var endDate = LocalDate.parse("2025-01-01");
    var startDate = endDate.minusYears(1);

    var weatherData = WeatherService.getWeatherData(paris, startDate, endDate);

    System.out.println(WeatherComputation.compute(weatherData));
  }
}
