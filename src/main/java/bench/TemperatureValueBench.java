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

import java.util.concurrent.TimeUnit;

// Benchmark                                       Mode  Cnt      Score      Error  Units
// Identity   TemperatureValueBench.computeMinMax  avgt    5  41814,510 ± 2631,829  ns/op
// Value      TemperatureValueBench.computeMinMax  avgt    5  10235,281 ± 170,167  ns/op

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = "--enable-preview")
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class TemperatureValueBench {

  private static final int ELEMENTS_COUNT = 10_000;
  Temperature[] temperatures = new Temperature[ELEMENTS_COUNT];
  {
    for (int i = 0; i < ELEMENTS_COUNT; i++) {
      temperatures[i] = new Temperature(i);
    }
  }

  /*value*/ record Temperature(float value) {
    public Temperature min(Temperature temperature) {
      return new Temperature(Math.min(value, temperature.value));
    }

    public Temperature max(Temperature temperature) {
      return new Temperature(Math.max(value, temperature.value));
    }
  }

  @Benchmark
  public void computeMinMax() {
    Temperature maxTemperature = new Temperature(Float.MIN_VALUE);
    Temperature minTemperature = new Temperature(Float.MAX_VALUE);

    for (int i = 0; i < ELEMENTS_COUNT; i++) {
      Temperature temperature = temperatures[i];
      maxTemperature = maxTemperature.max(temperature);
      minTemperature = minTemperature.min(temperature);
    }
  }
}
