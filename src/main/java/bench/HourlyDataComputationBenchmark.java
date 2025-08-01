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
import java.util.concurrent.TimeUnit;

// Benchmark                                                      Mode  Cnt     Score    Error  Units
// HourlyDataComputationBenchmark.identityComputation             avgt    5  1978,858 ± 12,276  us/op
// HourlyDataComputationBenchmark.primitiveComputation            avgt    5   154,205 ±  1,536  us/op
// HourlyDataComputationBenchmark.valueComputation                avgt    5   801,330 ± 15,198  us/op
// HourlyDataComputationBenchmark.valueNullRestrictedComputation  avgt    5   181,246 ±  0,296  us/op
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(
    value = 1,
    jvmArgs = {"--enable-preview"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class HourlyDataComputationBenchmark {

  // Ten years, 2025-2015
  private final LocalDate endDate = LocalDate.parse("2025-01-01");
  private final LocalDate startDate = endDate.minusYears(20);

  private identity.weather.WeatherService.HourlyData identityHourlyData;
  {
    var paris = new identity.weather.WeatherService.LatLong(48.864716, 2.349014);
    try {
      identityHourlyData = identity.weather.WeatherService.getHourlyData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private primitive.weather.WeatherService.HourlyData primitiveHourlyData;
  {
    var paris = new primitive.weather.WeatherService.LatLong(48.864716, 2.349014);
    try {
      primitiveHourlyData = primitive.weather.WeatherService.getHourlyData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private value.weather.WeatherService.HourlyData valueHourlyData;
  {
    var paris = new value.weather.WeatherService.LatLong(48.864716, 2.349014);
    try {
      valueHourlyData = value.weather.WeatherService.getHourlyData(paris, startDate, endDate);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  //@Benchmark
  public identity.weather.WeatherComputation.WeatherResult identityComputation() {
    return identity.weather.WeatherComputation.computeHourlyData(identityHourlyData);
  }

  //@Benchmark
  public primitive.weather.WeatherComputation.WeatherResult primitiveComputation() {
    return primitive.weather.WeatherComputation.computeHourlyData(primitiveHourlyData);
  }

  //@Benchmark
  public value.weather.WeatherComputation.WeatherResult valueComputation() {
    return value.weather.WeatherComputation.computeHourlyData(valueHourlyData);
  }

  //@Benchmark
  @Fork(value = 1, jvmArgs = {"--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public value.weather.WeatherComputation.WeatherResult valueNullRestrictedComputation() {
    return value.weather.WeatherComputation.computeHourlyData(valueHourlyData);
  }
}