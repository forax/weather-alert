package weather;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherComputationTest {

  // helper methods
  private static Object unwrap(Object value)  {
    if (!(value instanceof Record record)) {
      return value;
    }
    var recordComponents = record.getClass().getRecordComponents();
    assert recordComponents.length == 1;
    try {
      return recordComponents[0].getAccessor().invoke(record);
    } catch (IllegalAccessException e) {
      throw (IllegalAccessError) new IllegalAccessError().initCause(e);
    } catch (InvocationTargetException e) {
      throw rethrow(e);
    }
  }

  private static Object wrap(Class<?> componentClass, float value) {
    try {
      var constructor = componentClass.getConstructor(float.class);
      return constructor.newInstance(value);
    } catch (InvocationTargetException e) {
      throw rethrow(e);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static AssertionError rethrow(InvocationTargetException e) {
    var cause = e.getCause();
    if (cause instanceof RuntimeException runtimeException) {
      throw runtimeException;
    }
    if (cause instanceof Error error) {
      throw error;
    }
    throw new AssertionError(cause);
  }



  // Test data provider for all three implementations
  static Stream<WeatherComputationFactory> weatherComputationImplementations() {
    return Stream.of(
        new WeatherComputationFactory(identity.weather.WeatherComputation.class),
        new WeatherComputationFactory(primitive.weather.WeatherComputation.class),
        new WeatherComputationFactory(value.weather.WeatherComputation.class));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("weatherComputationImplementations")
  public void testComputeWeatherDataWithSampleData(WeatherComputationFactory factory) {
    var weatherDataList = List.of(
        WeatherComputationFactory.createWeatherData(20.5f, 15.2f, 2.3f, factory.weatherComputationClass),
        WeatherComputationFactory.createWeatherData(25.0f, 10.5f, 1.2f, factory.weatherComputationClass));
    var result = factory.computeWeatherData(weatherDataList);

    // Verify the result contains expected computations
    assertEquals(20.5f, unwrap(factory.getWeatherDataAccessor(result, "minTemperature")));
    assertEquals(25.0f, unwrap(factory.getWeatherDataAccessor(result, "maxTemperature")));
    assertEquals(15.2f, unwrap(factory.getWeatherDataAccessor(result, "maxWindspeed")));
    assertEquals(3.5f, unwrap(factory.getWeatherDataAccessor(result, "totalPrecipitation")));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("weatherComputationImplementations")
  public void testComputeWeatherDataWithEmptyList(WeatherComputationFactory factory) {
    var emptyList = List.of();
    var result = factory.computeWeatherData(emptyList);
    assertNotNull(result);

    // For empty list, should return initial values
    var minTemperature = factory.getWeatherDataAccessor(result, "minTemperature");
    var maxTemperature = factory.getWeatherDataAccessor(result, "maxTemperature");
    var maxWindspeed = factory.getWeatherDataAccessor(result, "maxWindspeed");
    var totalPrecipitation = factory.getWeatherDataAccessor(result, "totalPrecipitation");

    assertEquals(Float.MAX_VALUE, unwrap(minTemperature));
    assertEquals(Float.MIN_VALUE, unwrap(maxTemperature));
    assertEquals(0f, unwrap(maxWindspeed));
    assertEquals(0f, unwrap(totalPrecipitation));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("weatherComputationImplementations")
  public void testComputeWeatherDataWithNullList(WeatherComputationFactory factory) {
    assertThrows(NullPointerException.class, () -> factory.computeWeatherData(null));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("weatherComputationImplementations")
  public void testComputeHourlyDataWithSampleData(WeatherComputationFactory factory) {
    var hourlyData = WeatherComputationFactory.createHourlyData(
        new float[]{20.5f, 25.0f, 18.3f},
        new float[]{15.2f, 10.5f, 12.1f},
        new float[]{2.3f, 1.2f, 0.8f},
        factory.weatherComputationClass);
    var result = factory.computeHourlyData(hourlyData);

    assertEquals(18.3f, unwrap(factory.getWeatherDataAccessor(result, "minTemperature")));
    assertEquals(25.0f, unwrap(factory.getWeatherDataAccessor(result, "maxTemperature")));
    assertEquals(15.2f, unwrap(factory.getWeatherDataAccessor(result, "maxWindspeed")));
    assertEquals(4.3f, unwrap(factory.getWeatherDataAccessor(result, "totalPrecipitation")));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("weatherComputationImplementations")
  public void testComputeHourlyDataWithMismatchedSizes(WeatherComputationFactory factory) {
    var invalidHourlyData = WeatherComputationFactory.createHourlyData(
        new float[]{20.5f, 25.0f},   // 2 elements
        new float[]{15.2f, 10.5f, 12.1f},        // 3 elements - mismatch!
        new float[]{2.3f, 1.2f},                 // 2 elements
        factory.weatherComputationClass);
    
    assertThrows(IllegalStateException.class, () -> factory.computeHourlyData(invalidHourlyData));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("weatherComputationImplementations")
  public void testWeatherDataProperties(WeatherComputationFactory factory) {
    var weatherData = WeatherComputationFactory.createWeatherData(24f, 15f, 2f, factory.weatherComputationClass);

    assertEquals(24f, unwrap(factory.getWeatherDataAccessor(weatherData, "temperature")));
    assertEquals(15f, unwrap(factory.getWeatherDataAccessor(weatherData, "windspeed")));
    assertEquals(2f, unwrap(factory.getWeatherDataAccessor(weatherData, "precipitation")));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("weatherComputationImplementations")
  public void testToWeatherData(WeatherComputationFactory factory) {
    var hourlyData = WeatherComputationFactory.createHourlyData(
        new float[]{20.5f, 25.0f, 18.3f},
        new float[]{15.2f, 10.5f, 12.1f},
        new float[]{2.3f, 1.2f, 0.8f},
        factory.weatherComputationClass);
    var weatherDataList = factory.toWeatherData(hourlyData);

    assertEquals(3, weatherDataList.size());
    
    // Verify the first weather data item
    var firstWeatherData = weatherDataList.getFirst();
    assertEquals(20.5f, unwrap(factory.getWeatherDataAccessor(firstWeatherData, "temperature")));
    assertEquals(15.2f, unwrap(factory.getWeatherDataAccessor(firstWeatherData, "windspeed")));
    assertEquals(2.3f, unwrap(factory.getWeatherDataAccessor(firstWeatherData, "precipitation")));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("weatherComputationImplementations")
  public void testWeatherResultEquality(WeatherComputationFactory factory) {
    var weatherDataList = List.of(
        WeatherComputationFactory.createWeatherData(20.5f, 15.2f, 2.3f, factory.weatherComputationClass),
        WeatherComputationFactory.createWeatherData(25.0f, 10.5f, 1.2f, factory.weatherComputationClass));
    
    var result1 = factory.computeWeatherData(weatherDataList);
    var result2 = factory.computeWeatherData(weatherDataList);
    
    // Verify that computing with the same data produces equal results
    assertEquals(result1, result2);
    assertEquals(result1.hashCode(), result2.hashCode());
  }


  public record WeatherComputationFactory(Class<?> weatherComputationClass) {
    private static Class<?> weatherServiceClassOf(String packageName, String className) throws ClassNotFoundException {
      return Class.forName(packageName + ".WeatherService$" + className);
    }

    private static Object createWeatherData(float temperature, float windspeed, float precipitation, Class<?> weatherComputationClass) {
      try {
        var packageName = weatherComputationClass.getPackageName();
        var weatherDataClass = Class.forName(packageName + ".WeatherComputation$WeatherData");

        if (packageName.equals("primitive.weather")) {
          // For primitive implementation: WeatherData(float temperature, float windspeed, float precipitation)
          var constructor = weatherDataClass.getConstructor(float.class, float.class, float.class);
          return constructor.newInstance(temperature, windspeed, precipitation);
        } else {
          // For identity and value implementations: need Temperature, Windspeed, Precipitation objects
          var temperatureClass = weatherServiceClassOf(packageName, "Temperature");
          var windspeedClass = weatherServiceClassOf(packageName, "Windspeed");
          var precipitationClass = weatherServiceClassOf(packageName, "Precipitation");

          var temperatureObject = wrap(temperatureClass, temperature);
          var windspeedObject = wrap(windspeedClass, windspeed);
          var precipitationObject = wrap(precipitationClass, precipitation);

          var constructor = weatherDataClass.getConstructor(temperatureClass, windspeedClass, precipitationClass);
          return constructor.newInstance(temperatureObject, windspeedObject, precipitationObject);
        }
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public static Object createHourlyData(float[] temperatures, float[] windspeeds, float[] precipitations, Class<?> weatherComputationClass) {
      try {
        var packageName = weatherComputationClass.getPackageName();
        var hourlyDataClass = weatherServiceClassOf(packageName, "HourlyData");

        if (packageName.equals("primitive.weather")) {
          // For primitive: HourlyData(float[] temperatures, float[] windspeeds, float[] precipitations)
          var constructor = hourlyDataClass.getConstructor(float[].class, float[].class, float[].class);
          return constructor.newInstance(temperatures, windspeeds, precipitations);
        } else {
          // For identity/value: HourlyData(List<Temperature>, List<Windspeed>, List<Precipitation>)
          var temperatureClass = weatherServiceClassOf(packageName, "Temperature");
          var windspeedClass = weatherServiceClassOf(packageName, "Windspeed");
          var precipitationClass = weatherServiceClassOf(packageName, "Precipitation");

          var temperatureList = IntStream.range(0, temperatures.length)
              .mapToObj(i -> wrap(temperatureClass, temperatures[i]))
              .toList();
          var windspeedList = IntStream.range(0, windspeeds.length)
              .mapToObj(i -> wrap(windspeedClass, windspeeds[i]))
              .toList();
          var precipitationList = IntStream.range(0, precipitations.length)
              .mapToObj(i -> wrap(precipitationClass, precipitations[i]))
              .toList();
          var constructor = hourlyDataClass.getConstructor(List.class, List.class, List.class);
          return constructor.newInstance(temperatureList, windspeedList, precipitationList);
        }
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public Object computeWeatherData(List<?> weatherDataList) {
      try {
        var method = weatherComputationClass.getMethod("computeWeatherData", List.class);
        return method.invoke(null, weatherDataList);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public Object computeHourlyData(Object hourlyData) {
      try {
        var method = weatherComputationClass.getMethod("computeHourlyData", hourlyData.getClass());
        return method.invoke(null, hourlyData);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public List<?> toWeatherData(Object hourlyData) {
      try {
        var method = weatherComputationClass.getMethod("toWeatherData", hourlyData.getClass());
        return (List<?>) method.invoke(null, hourlyData);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public Object getWeatherResultAccessor(Object weatherResult, String componentName) {
      try {
        var method = weatherResult.getClass().getMethod(componentName);
        return method.invoke(weatherResult);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public Object getWeatherDataAccessor(Object weatherData, String componentName) {
      try {
        var method = weatherData.getClass().getMethod(componentName);
        return method.invoke(weatherData);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }
  }
}