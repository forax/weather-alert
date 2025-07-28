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

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"--enable-preview"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class Float16Benchmark {

  public value record Temperature32(float value) {
    @Override
    public String toString() {
      return value + " °C";
    }

    public Temperature32 min(Temperature32 temperature) {
      return new Temperature32(Math.min(value, temperature.value));
    }

    public Temperature32 max(Temperature32 temperature) {
      return new Temperature32(Math.max(value, temperature.value));
    }
  }

  public static final value class Temperature16 {
    private final short value;  // floatBinary16

    private Temperature16(short value) {
      this.value = value;
      super();
    }

    public Temperature16(float value) {
      this(Float.floatToFloat16(value));
    }

    public float value() {
      return Float.float16ToFloat(value);
    }

    public Temperature32 to32() {
      return new Temperature32(value());
    }

    @Override
    public String toString() {
      return value() + " °C";
    }

    public Temperature16 min(Temperature16 temperature) {
      return new Temperature16(Math.min(value(), temperature.value()));
    }

    public Temperature16 max(Temperature16 temperature) {
      return new Temperature16(Math.max(value(), temperature.value()));
    }
  }

  private final Temperature16[] temperature16s;
  private final Temperature32[] temperature32s;
  {
    var random = new Random(0);
    temperature16s = new Temperature16[1024];
    temperature32s = new Temperature32[1024];
    for(int i = 0; i < 1024; i++) {
      var value = random.nextFloat();
      temperature16s[i] = new Temperature16(value);
      temperature32s[i] = new Temperature32(value);
    }
  }

  //@Benchmark
  public float min32() {
    var min = temperature32s[0];
    for (var i = 1; i < temperature32s.length; i++) {
      min = min.min(temperature32s[i]);
    }
    return min.value();
  }

  //@Benchmark
  public float min16() {
    var min = temperature16s[0];
    for (var i = 1; i < temperature16s.length; i++) {
      min = min.min(temperature16s[i]);
    }
    return min.value();
  }

  //@Benchmark
  public float minMixed() {
    var min = temperature16s[0].to32();
    for (var i = 1; i < temperature16s.length; i++) {
      min = min.min(temperature16s[i].to32());
    }
    return min.value();
  }
}