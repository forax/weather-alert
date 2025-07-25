package weather;

import weather.WeatherService.*;

import java.util.List;

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
    var result = new WeatherResult(new Temperature(Float.MAX_VALUE), new Temperature(Float.MIN_VALUE), new Windspeed(0f), new Precipitation(0f));
    for(var weatherData : weatherDataList) {
      result = result.compute(weatherData);
    }
    return result;
  }
}
