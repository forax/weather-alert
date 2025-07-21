package weather;

import java.io.IOException;

// Example usage class
class WindForecastExample {
  public static void main(String[] args) throws IOException {
    // Paris coordinates
    var parisLatitude = "48.864716";
    var parisLongitude = "2.349014";

    var windSpeedList = WindForecastService.getWindSpeedForecast(parisLatitude, parisLongitude);

    System.out.println("Wind speed forecasts for Paris:");
    System.out.println(windSpeedList.getClass());
    windSpeedList.forEach(System.out::println);
  }
}
