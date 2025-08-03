package value.weather;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jdk.internal.vm.annotation.NullRestricted;
import util.GenericValueList;
import util.ValueList;
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

  //private static final AggregateList.Factory<WeatherData> AGGREGATE_LIST_FACTORY =
  //    AggregateList.factory(MethodHandles.lookup(), WeatherData.class);
  private static final ValueList.Factory<WeatherData> VALUE_LIST_FACTORY =
      ValueList.factory(MethodHandles.lookup(), WeatherData.class);

  //public value record WeatherData(Temperature temperature, Windspeed windspeed, Precipitation precipitation) { }
  public value record WeatherData(@NullRestricted Temperature temperature, @NullRestricted Windspeed windspeed, @NullRestricted Precipitation precipitation) { }

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
        .collect(Collectors.toCollection(() -> VALUE_LIST_FACTORY.create(precipitations.size())));
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
}
