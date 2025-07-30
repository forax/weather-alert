package value.weather;

import java.util.List;
import value.weather.WeatherService.*;

public class WeatherComputation {
  public value record WeatherResult(Temperature minTemperature, Temperature maxTemperature, Windspeed maxWindspeed, Precipitation totalPrecipitation) {
    WeatherResult compute(WeatherData data) {
      return new WeatherResult(
          minTemperature.min(data.temperature()),
          maxTemperature.max(data.temperature()),
          maxWindspeed.max(data.windspeed()),
          totalPrecipitation.add(data.precipitation()));
    }
  }

  public static WeatherResult compute(List<WeatherData> weatherDataList) {
    var start = System.nanoTime();
    var result = new WeatherResult(
        new Temperature(Float.MAX_VALUE),
        new Temperature(Float.MIN_VALUE),
        new Windspeed(0f),
        new Precipitation(0f));
    for(var weatherData : weatherDataList) {
      result = result.compute(weatherData);
    }
    var end = System.nanoTime();
    System.out.println("Computation time: " + (end - start) + " ns");
    return result;
  }
}
