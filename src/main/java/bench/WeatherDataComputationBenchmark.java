package bench;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

// Benchmark                                                       Mode  Cnt     Score   Error  Units
// WeatherDataComputationBenchmark.identityComputation             avgt    5  2130,118 ± 9,767  us/op
// WeatherDataComputationBenchmark.primitiveComputation            avgt    5   155,823 ± 0,595  us/op
// WeatherDataComputationBenchmark.valueComputation                avgt    5   155,767 ± 0,399  us/op
// WeatherDataComputationBenchmark.valueNullRestrictedComputation  avgt    5   182,016 ± 2,376  us/o
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
  @Fork(value = 1, jvmArgs = {"--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public value.weather.WeatherComputation.WeatherResult valueNullRestrictedComputation() {
    return value.weather.WeatherComputation.computeWeatherData(valueWeatherDataList);
  }
}