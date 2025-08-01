package bench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Benchmark                                             Mode  Cnt     Score     Error  Units
// ComputattionBenchmark.identityComputation             avgt    5  2156,882 ± 117,790  us/op
// ComputattionBenchmark.primitiveComputation            avgt    5   155,927 ±   0,739  us/op
// ComputattionBenchmark.valueComputation                avgt    5   155,771 ±   0,727  us/op
// ComputattionBenchmark.valueNullRestrictedComputation  avgt    5   181,904 ±   1,371  us/op
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(
    value = 1,
    jvmArgs = {"--enable-preview"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ComputattionBenchmark {

  // Ten years, 2025-2015
  private final LocalDate endDate = LocalDate.parse("2025-01-01");
  private final LocalDate  startDate = endDate.minusYears(20);

  private List<identity.weather.WeatherService.WeatherData> identityWeatherDataList;
  {
    var paris = new identity.weather.WeatherService.LatLong(48.864716, 2.349014);
    try {
      identityWeatherDataList = identity.weather.WeatherService.getWeatherData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private List<primitive.weather.WeatherService.WeatherData> primitiveWeatherDataList;
  {
    var paris = new primitive.weather.WeatherService.LatLong(48.864716, 2.349014);
    try {
      primitiveWeatherDataList = primitive.weather.WeatherService.getWeatherData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private List<value.weather.WeatherService.WeatherData> valueWeatherDataList;
  {
    var paris = new value.weather.WeatherService.LatLong(48.864716, 2.349014);
    try {
      valueWeatherDataList = value.weather.WeatherService.getWeatherData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @Benchmark
  public identity.weather.WeatherComputation.WeatherResult identityComputation() {
    return identity.weather.WeatherComputation.compute(identityWeatherDataList);
  }

  @Benchmark
  public primitive.weather.WeatherComputation.WeatherResult primitiveComputation() {
    return primitive.weather.WeatherComputation.compute(primitiveWeatherDataList);
  }

  @Benchmark
  public value.weather.WeatherComputation.WeatherResult valueComputation() {
    return value.weather.WeatherComputation.compute(valueWeatherDataList);
  }

  @Benchmark
  @Fork(value = 1, jvmArgs = {"--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public value.weather.WeatherComputation.WeatherResult valueNullRestrictedComputation() {
    return value.weather.WeatherComputation.compute(valueWeatherDataList);
  }
}