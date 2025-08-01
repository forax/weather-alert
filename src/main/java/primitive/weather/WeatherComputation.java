package primitive.weather;

import java.util.List;
import primitive.weather.WeatherService.*;

public class WeatherComputation {
  public record WeatherResult(float minTemperature, float maxTemperature, float maxWindspeed, float totalPrecipitation) { }

  public static WeatherResult compute(List<WeatherData> weatherDataList) {
    var minTemperature = Float.MAX_VALUE;
    var maxTemperature = Float.MIN_VALUE;
    var maxWindspeed = 0.0f;
    var totalPrecipitation = 0.0f;
    for(var weatherData : weatherDataList) {
      minTemperature = Math.min(minTemperature, weatherData.temperature());
      maxTemperature = Math.max(maxTemperature, weatherData.temperature());
      maxWindspeed = Math.max(maxWindspeed, weatherData.windspeed());
      totalPrecipitation += weatherData.precipitation();
    }
    return new WeatherResult(minTemperature, maxTemperature, maxWindspeed, totalPrecipitation);
  }
}
