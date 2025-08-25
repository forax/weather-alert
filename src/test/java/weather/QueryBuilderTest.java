package weather;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class QueryBuilderTest {

  // Test data provider for all three implementations
  static Stream<QueryBuilderFactory> queryBuilderImplementations() {
    return Stream.of(
        new QueryBuilderFactory(identity.weather.QueryBuilder.class),
        new QueryBuilderFactory(primitive.weather.QueryBuilder.class),
        new QueryBuilderFactory(value.weather.QueryBuilder.class));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("queryBuilderImplementations")
  public void testConstructorWithAllParameters(QueryBuilderFactory factory) {
    var latLong = factory.createLatLong(52.5, 13.4);
    var startDate = LocalDate.of(2023, 1, 1);
    var endDate = LocalDate.of(2023, 1, 2);

    var queryBuilder = factory.createQueryBuilder(latLong, startDate, endDate);
    var uri = factory.toURI(queryBuilder);

    assertNotNull(queryBuilder);
    assertEquals(
        "https://archive-api.open-meteo.com/v1/archive?latitude=52.5&longitude=13.4&start_date=2023-01-01&end_date=2023-01-02&hourly=temperature_2m,wind_speed_10m,precipitation",
        uri.toString());
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("queryBuilderImplementations")
  public void testConstructorWithLatLongOnly(QueryBuilderFactory factory) {
    var latLong = factory.createLatLong(52.5, 13.4);

    var queryBuilder = factory.createQueryBuilder(latLong);
    var uri = factory.toURI(queryBuilder);

    assertNotNull(queryBuilder);

    // Calculate expected dates dynamically based on the current date
    var now = LocalDate.now();
    var yesterday = now.minusDays(1);
    var expectedUri =
        "https://archive-api.open-meteo.com/v1/archive?latitude=52.5&longitude=13.4&start_date=%s&end_date=%s&hourly=temperature_2m,wind_speed_10m,precipitation"
            .formatted(yesterday, now);

    assertEquals(expectedUri, uri.toString());
  }


  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("queryBuilderImplementations")
  public void testConstructorWithNullLatLong(QueryBuilderFactory factory) {
    var startDate = LocalDate.of(2023, 1, 1);
    var endDate = LocalDate.of(2023, 1, 2);

    assertThrows(
        NullPointerException.class, () -> factory.createQueryBuilder(null, startDate, endDate));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("queryBuilderImplementations")
  public void testConstructorWithNullStartDate(QueryBuilderFactory factory) {
    var latLong = factory.createLatLong(52.5, 13.4);
    var endDate = LocalDate.of(2023, 1, 2);

    assertThrows(
        NullPointerException.class, () -> factory.createQueryBuilder(latLong, null, endDate));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("queryBuilderImplementations")
  public void testConstructorWithNullEndDate(QueryBuilderFactory factory) {
    var latLong = factory.createLatLong(52.5, 13.4);
    var startDate = LocalDate.of(2023, 1, 1);

    assertThrows(
        NullPointerException.class, () -> factory.createQueryBuilder(latLong, startDate, null));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("queryBuilderImplementations")
  public void testConstructorWithInvalidDateRange(QueryBuilderFactory factory) {
    var latLong = factory.createLatLong(52.5, 13.4);
    var startDate = LocalDate.of(2023, 1, 2);
    var endDate = LocalDate.of(2023, 1, 1); // endDate before startDate

    assertThrows(
        IllegalArgumentException.class,
        () -> factory.createQueryBuilder(latLong, startDate, endDate));
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("queryBuilderImplementations")
  public void testDateRange(QueryBuilderFactory factory) {
    var latLong = factory.createLatLong(52.5, 13.4);
    var initialStartDate = LocalDate.of(2023, 1, 1);
    var initialEndDate = LocalDate.of(2023, 1, 2);
    var newStartDate = LocalDate.of(2023, 2, 1);
    var newEndDate = LocalDate.of(2023, 2, 2);

    var queryBuilder = factory.createQueryBuilder(latLong, initialStartDate, initialEndDate);
    var newQueryBuilder = factory.dateRange(queryBuilder, newStartDate, newEndDate);
    var uri = factory.toURI(newQueryBuilder);

    assertNotNull(newQueryBuilder);
    assertEquals(
        "https://archive-api.open-meteo.com/v1/archive?latitude=52.5&longitude=13.4&start_date=2023-02-01&end_date=2023-02-02&hourly=temperature_2m,wind_speed_10m,precipitation",
        uri.toString());
  }

  @ParameterizedTest(name = "{0} implementation")
  @MethodSource("queryBuilderImplementations")
  public void testToURI(QueryBuilderFactory factory) {
    var latLong = factory.createLatLong(52.5, 13.4);
    var startDate = LocalDate.of(2023, 1, 1);
    var endDate = LocalDate.of(2023, 1, 2);

    var queryBuilder = factory.createQueryBuilder(latLong, startDate, endDate);
    var uri = factory.toURI(queryBuilder);

    assertNotNull(uri);
    assertEquals(
        "https://archive-api.open-meteo.com/v1/archive?latitude=52.5&longitude=13.4&start_date=2023-01-01&end_date=2023-01-02&hourly=temperature_2m,wind_speed_10m,precipitation",
        uri.toString());
  }

  public record QueryBuilderFactory(Class<?> queryBuilderClass) {

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

    public Object createLatLong(double latitude, double longitude) {
      try {
        var packageName = queryBuilderClass.getPackageName();
        var latLongClass = Class.forName(packageName + ".WeatherService$LatLong");
        var constructor = latLongClass.getConstructor(double.class, double.class);
        return constructor.newInstance(latitude, longitude);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public Object createQueryBuilder(Object latLong) {
      try {
        var constructor = queryBuilderClass.getConstructor(latLong.getClass());
        return constructor.newInstance(latLong);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public Object createQueryBuilder(Object latLong, LocalDate startDate, LocalDate endDate) {
      try {
        var constructor =
            queryBuilderClass.getConstructor(latLong.getClass(), LocalDate.class, LocalDate.class);
        return constructor.newInstance(latLong, startDate, endDate);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public Object dateRange(Object queryBuilder, LocalDate startDate, LocalDate endDate) {
      try {
        var method = queryBuilderClass.getMethod("dateRange", LocalDate.class, LocalDate.class);
        return method.invoke(queryBuilder, startDate, endDate);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }

    public URI toURI(Object queryBuilder) {
      try {
        var method = queryBuilderClass.getMethod("toURI");
        return (URI) method.invoke(queryBuilder);
      } catch (InvocationTargetException e) {
        throw rethrow(e);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }
  }
}
