package bench;

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
import util.FlatListFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Benchmark - Specialized                                        Mode  Cnt     Score    Error  Units
// HourlyDataComputationBenchmark.identityComputation             avgt    5  1978,127 ± 14,217  us/op
// HourlyDataComputationBenchmark.primitiveComputation            avgt    5   151,743 ±  0,980  us/op
// HourlyDataComputationBenchmark.valueComputation                avgt    5   778,932 ± 18,041  us/op
// HourlyDataComputationBenchmark.valueNullRestrictedComputation  avgt    5   177,009 ±  0,973  us/op
// HourlyDataComputationBenchmark.valueNullableComputation        avgt    5   490,299 ±  0,611  us/op

// Benchmark - Generic                                            Mode  Cnt      Score     Error  Units
// HourlyDataComputationBenchmark.identityComputation             avgt    5   1985,475 ±  20,515  us/op
// HourlyDataComputationBenchmark.primitiveComputation            avgt    5    151,956 ±   1,624  us/op
// HourlyDataComputationBenchmark.valueComputation                avgt    5    685,881 ±  14,699  us/op
// HourlyDataComputationBenchmark.valueNullRestrictedComputation  avgt    5  31843,872 ± 402,694  us/op
// HourlyDataComputationBenchmark.valueNullableComputation        avgt    5  31005,588 ± 447,575  us/op
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(
    value = 1,
    jvmArgs = {
        "--enable-preview",
        "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
        "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
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
    checkFlat(valueHourlyData.temperatures());
    checkFlat(valueHourlyData.windspeeds());
    checkFlat(valueHourlyData.precipitations());
  }

  private static void checkFlat(List<?> list) {
    if (!(FlatListFactory.isFlat(list))) {
      throw new AssertionError("list is not a FlatList");
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
  public value.weather.WeatherComputation.WeatherResult valueNullableComputation() {
    return value.weather.WeatherComputation.computeHourlyData(valueHourlyData);
  }

  //@Benchmark
  public value.weather.WeatherComputation.WeatherResult valueNullRestrictedComputation() {
    return value.weather.WeatherComputation.computeHourlyData(valueHourlyData);
  }
}