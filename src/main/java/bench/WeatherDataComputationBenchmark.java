package bench;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jdk.internal.value.ValueClass;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import util.AggregateList;
import util.FlatList;

// Benchmark                                                                Mode  Cnt     Score    Error  Units
// WeatherDataComputationBenchmark.aggregateValueComputation                avgt    5  1950,894 ± 58,790  us/op
// WeatherDataComputationBenchmark.aggregateValueNullRestrictedComputation  avgt    5   176,937 ±  0,341  us/op
// WeatherDataComputationBenchmark.aggregateValueNullableComputation        avgt    5   478,109 ±  2,462  us/op
// WeatherDataComputationBenchmark.identityComputation                      avgt    5  2143,667 ± 30,305  us/op
// WeatherDataComputationBenchmark.primitiveComputation                     avgt    5   155,506 ±  0,268  us/op
// WeatherDataComputationBenchmark.valueComputation                         avgt    5   155,449 ±  0,445  us/op
// WeatherDataComputationBenchmark.valueNullRestrictedComputation           avgt    5   156,090 ±  0,821  us/op
// WeatherDataComputationBenchmark.valueNullableComputation                 avgt    5   156,043 ±  0,501  us/op
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(
    value = 1,
    jvmArgs = {"--enable-preview"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class WeatherDataComputationBenchmark {

  // Ten years, 2025-2015
  private final LocalDate endDate = LocalDate.parse("2025-01-01");
  private final LocalDate startDate = endDate.minusYears(20);

  private static void checkFlatIfAvailable(List<?> list) {
    if (!(list instanceof FlatList<?> flatList)) {
      throw new AssertionError("list is not a FlatList");
    }
    try {
      var _ = ValueClass.class;
    } catch(IllegalAccessError _) {
      return;  // okay !
    }
    if (!flatList.isFlat()) {
      throw new AssertionError("list array is not flat");
    }
  }

  private List<identity.weather.WeatherComputation.WeatherData> identityWeatherDataList;
  {
    var paris = new identity.weather.WeatherService.LatLong(48.864716, 2.349014);
    identity.weather.WeatherService.HourlyData hourlyData;
    try {
      hourlyData = identity.weather.WeatherService.getHourlyData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    identityWeatherDataList = identity.weather.WeatherComputation.toWeatherData(hourlyData);
  }

  private List<primitive.weather.WeatherComputation.WeatherData> primitiveWeatherDataList;
  {
    var paris = new primitive.weather.WeatherService.LatLong(48.864716, 2.349014);
    primitive.weather.WeatherService.HourlyData hourlyData;
    try {
      hourlyData = primitive.weather.WeatherService.getHourlyData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    primitiveWeatherDataList = primitive.weather.WeatherComputation.toWeatherData(hourlyData);
  }

  private List<value.weather.WeatherComputation.WeatherData> valueWeatherDataList;
  {
    var paris = new value.weather.WeatherService.LatLong(48.864716, 2.349014);
    value.weather.WeatherService.HourlyData hourlyData;
    try {
      hourlyData = value.weather.WeatherService.getHourlyData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    valueWeatherDataList = value.weather.WeatherComputation.toWeatherData(hourlyData);
    // checkFlatIfAvailable(valueWeatherDataList);  // @LooselyConsistentValue does not seem to work ??
  }

  private List<value.weather.WeatherComputation.WeatherData> aggregateValueWeatherDataList;
  {
    var paris = new value.weather.WeatherService.LatLong(48.864716, 2.349014);
    value.weather.WeatherService.HourlyData hourlyData;
    try {
      hourlyData = value.weather.WeatherService.getHourlyData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    aggregateValueWeatherDataList =
        AggregateList.factory(MethodHandles.lookup(), value.weather.WeatherComputation.WeatherData.class)
            .create(hourlyData.temperatures(), hourlyData.windspeeds(), hourlyData.precipitations());
  }


  @Benchmark
  public identity.weather.WeatherComputation.WeatherResult identityComputation() {
    return identity.weather.WeatherComputation.computeWeatherData(identityWeatherDataList);
  }

  @Benchmark
  public primitive.weather.WeatherComputation.WeatherResult primitiveComputation() {
    return primitive.weather.WeatherComputation.computeWeatherData(primitiveWeatherDataList);
  }

  @Benchmark
  public value.weather.WeatherComputation.WeatherResult valueComputation() {
    return value.weather.WeatherComputation.computeWeatherData(valueWeatherDataList);
  }

  @Benchmark
  @Fork(value = 1, jvmArgs = {"--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED"})
  public value.weather.WeatherComputation.WeatherResult valueNullableComputation() {
    return value.weather.WeatherComputation.computeWeatherData(valueWeatherDataList);
  }

  @Benchmark
  @Fork(value = 1, jvmArgs = {"--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public value.weather.WeatherComputation.WeatherResult valueNullRestrictedComputation() {
    return value.weather.WeatherComputation.computeWeatherData(valueWeatherDataList);
  }


  //@Benchmark
  public value.weather.WeatherComputation.WeatherResult aggregateValueComputation() {
    return value.weather.WeatherComputation.computeWeatherData(aggregateValueWeatherDataList);
  }

  //@Benchmark
  @Fork(value = 1, jvmArgs = {"--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED"})
  public value.weather.WeatherComputation.WeatherResult aggregateValueNullableComputation() {
    return value.weather.WeatherComputation.computeWeatherData(aggregateValueWeatherDataList);
  }

  //@Benchmark
  @Fork(value = 1, jvmArgs = {"--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public value.weather.WeatherComputation.WeatherResult aggregateValueNullRestrictedComputation() {
    return value.weather.WeatherComputation.computeWeatherData(aggregateValueWeatherDataList);
  }
}