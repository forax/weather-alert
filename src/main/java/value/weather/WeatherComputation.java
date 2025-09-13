package value.weather;

import jdk.internal.vm.annotation.NullRestricted;
import util.FlatListFactory;
import value.weather.WeatherService.HourlyData;
import value.weather.WeatherService.Precipitation;
import value.weather.WeatherService.Temperature;
import value.weather.WeatherService.Windspeed;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WeatherComputation {

  //public value record WeatherData(Temperature temperature, Windspeed windspeed, Precipitation precipitation) { }
  //@LooselyConsistentValue
  public value record WeatherData(
    @NullRestricted Temperature temperature,
    @NullRestricted Windspeed windspeed,
    @NullRestricted Precipitation precipitation) { }

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
      //.toList();
      //.collect(Collectors.toCollection(() -> new GenericValueList<>(WeatherData.class, precipitations.size())));
      .collect(Collectors.toCollection(() -> FlatListFactory.create(WeatherData.class)));
  }

  public value record WeatherResult(Temperature minTemperature, Temperature maxTemperature, Windspeed maxWindspeed, Precipitation totalPrecipitation) {
    WeatherResult compute(WeatherData data) {
      return new WeatherResult(
        minTemperature.min(data.temperature()),
        maxTemperature.max(data.temperature()),
        maxWindspeed.max(data.windspeed()),
        totalPrecipitation.add(data.precipitation()));
    }
  }

  public static WeatherResult computeWeatherData(List<WeatherData> weatherDataList) {
    var result = new WeatherResult(
      new Temperature(Float.MAX_VALUE),
      new Temperature(Float.MIN_VALUE),
      new Windspeed(0.0f),
      new Precipitation(0.0f));
    for(var weatherData : weatherDataList) {
      result = result.compute(weatherData);
    }
    return result;
  }


  public static WeatherResult computeHourlyData(HourlyData data) {
    var result = new WeatherResult(
      new Temperature(Float.MAX_VALUE),
      new Temperature(Float.MIN_VALUE),
      new Windspeed(0.0f),
      new Precipitation(0.0f));
    var temperatures = data.temperatures();
    var windspeeds = data.windspeeds();
    var precipitations = data.precipitations();
    if (temperatures.size() != windspeeds.size() || temperatures.size() != precipitations.size()) {
      throw new IllegalStateException("temperature size != windspeed size or precipitation size != precipitation size");
    }
    for(var i = 0; i < temperatures.size(); i++) {
      result = result.compute(new WeatherData(
        temperatures.get(i),
        windspeeds.get(i),
        precipitations.get(i)));
    }
    return result;
  }
}
