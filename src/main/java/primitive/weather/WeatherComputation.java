package primitive.weather;

import java.util.List;
import java.util.stream.IntStream;

import primitive.weather.WeatherService.*;

public class WeatherComputation {
  public record WeatherData(float temperature, float windspeed, float precipitation) { }

  public static List<WeatherData> toWeatherData(HourlyData data) {
    var temperatures = data.temperatures();
    var windspeeds = data.windspeeds();
    var precipitations = data.precipitations();
    if (temperatures.length != windspeeds.length || temperatures.length != precipitations.length) {
      throw new IllegalStateException("temperature size != windspeed size or precipitation size != precipitation size");
    }
    return  IntStream.range(0, precipitations.length)
        .mapToObj(i -> new WeatherData(
            temperatures[i],
            windspeeds[i],
            precipitations[i]))
        .toList();
  }

  public record WeatherResult(float minTemperature, float maxTemperature, float maxWindspeed, float totalPrecipitation) { }

  public static WeatherResult computeWeatherData(List<WeatherData> weatherDataList) {
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


  public static WeatherResult computeHourlyData(HourlyData data) {
    var minTemperature = Float.MAX_VALUE;
    var maxTemperature = Float.MIN_VALUE;
    var maxWindspeed = 0.0f;
    var totalPrecipitation = 0.0f;
    var temperatures = data.temperatures();
    var windspeeds = data.windspeeds();
    var precipitations = data.precipitations();
    if (temperatures.length != windspeeds.length || temperatures.length != precipitations.length) {
      throw new IllegalStateException("temperature size != windspeed size or precipitation size != precipitation size");
    }
    for(var i = 0; i < temperatures.length; i++) {
      minTemperature = Math.min(minTemperature,  temperatures[i]);
      maxTemperature = Math.max(maxTemperature,  temperatures[i]);
      maxWindspeed = Math.max(maxWindspeed, windspeeds[i]);
      totalPrecipitation += precipitations[i];
    }
    return new WeatherResult(minTemperature, maxTemperature, maxWindspeed, totalPrecipitation);
  }
}
