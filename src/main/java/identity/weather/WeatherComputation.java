package identity.weather;

import identity.weather.WeatherService.HourlyData;
import identity.weather.WeatherService.Precipitation;
import identity.weather.WeatherService.Temperature;
import identity.weather.WeatherService.Windspeed;

import java.util.List;
import java.util.stream.IntStream;

public class WeatherComputation {
  public record WeatherData(Temperature temperature, Windspeed windspeed, Precipitation precipitation) { }

  public static List<WeatherData> toWeatherData(HourlyData data) {
    var temperatures = data.temperatures();
    var windspeeds = data.windspeeds();
    var precipitations = data.precipitations();
    if (temperatures.size() != windspeeds.size() || temperatures.size() != precipitations.size()) {
      throw new IllegalStateException("temperature size != windspeed size or precipitation size != precipitation size");
    }
    return  IntStream.range(0, precipitations.size())
        .mapToObj(i -> new WeatherData(
            temperatures.get(i),
            windspeeds.get(i),
            precipitations.get(i)))
        .toList();
  }

  public record WeatherResult(Temperature minTemperature, Temperature maxTemperature, Windspeed maxWindspeed, Precipitation totalPrecipitation) { }

  public static WeatherResult computeWeatherData(List<WeatherData> weatherDataList) {
    var minTemperature = new Temperature(Float.MAX_VALUE);
    var maxTemperature = new Temperature(Float.MIN_VALUE);
    var maxWindspeed = new Windspeed(0.0f);
    var totalPrecipitation = new Precipitation(0.0f);
    for(var weatherData : weatherDataList) {
      minTemperature = minTemperature.min(weatherData.temperature());
      maxTemperature = maxTemperature.max(weatherData.temperature());
      maxWindspeed = maxWindspeed.max(weatherData.windspeed());
      totalPrecipitation = totalPrecipitation.add(weatherData.precipitation());
    }
    return new WeatherResult(minTemperature, maxTemperature, maxWindspeed, totalPrecipitation);
  }

  public static WeatherResult computeWeatherData(WeatherData[] weatherDataArray) {
    var minTemperature = new Temperature(Float.MAX_VALUE);
    var maxTemperature = new Temperature(Float.MIN_VALUE);
    var maxWindspeed = new Windspeed(0.0f);
    var totalPrecipitation = new Precipitation(0.0f);
    for(var weatherData : weatherDataArray) {
      minTemperature = minTemperature.min(weatherData.temperature());
      maxTemperature = maxTemperature.max(weatherData.temperature());
      maxWindspeed = maxWindspeed.max(weatherData.windspeed());
      totalPrecipitation = totalPrecipitation.add(weatherData.precipitation());
    }
    return new WeatherResult(minTemperature, maxTemperature, maxWindspeed, totalPrecipitation);
  }

  public static WeatherResult computeHourlyData(HourlyData data) {
    var minTemperature = new Temperature(Float.MAX_VALUE);
    var maxTemperature = new Temperature(Float.MIN_VALUE);
    var maxWindspeed = new Windspeed(0.0f);
    var totalPrecipitation = new Precipitation(0.0f);
    var temperatures = data.temperatures();
    var windspeeds = data.windspeeds();
    var precipitations = data.precipitations();
    if (temperatures.size() != windspeeds.size() || temperatures.size() != precipitations.size()) {
      throw new IllegalStateException("temperature size != windspeed size or precipitation size != precipitation size");
    }
    for(var i = 0; i < temperatures.size(); i++) {
      minTemperature = minTemperature.min(temperatures.get(i));
      maxTemperature = maxTemperature.max(temperatures.get(i));
      maxWindspeed = maxWindspeed.max(windspeeds.get(i));
      totalPrecipitation = totalPrecipitation.add(precipitations.get(i));
    }
    return new WeatherResult(minTemperature, maxTemperature, maxWindspeed, totalPrecipitation);
  }
}
