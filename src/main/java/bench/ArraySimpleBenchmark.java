
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

import java.util.concurrent.TimeUnit;

//Benchmark                                           Mode  Cnt    Score   Error  Units
//ArraySimpleBenchmark.sumErasedNullRestrictedValues  avgt    5  280,896 ± 1,253  ns/op
//ArraySimpleBenchmark.sumErasedNullableValues        avgt    5  304,782 ± 3,261  ns/op
//ArraySimpleBenchmark.sumErasedValues                avgt    5  305,109 ± 3,719  ns/op
//ArraySimpleBenchmark.sumGenericValues               avgt    5  344,292 ± 0,779  ns/op
//ArraySimpleBenchmark.sumNullRestrictedValues        avgt    5  280,420 ± 1,175  ns/op
//ArraySimpleBenchmark.sumNullableValues              avgt    5  305,104 ± 2,728  ns/op
//ArraySimpleBenchmark.sumValues                      avgt    5  305,705 ± 2,245  ns/op

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {
    "--enable-preview",
    "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
    "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ArraySimpleBenchmark {

  private Value[] values, nullRestrictedValues, nullableValues;
  private Object[] genericValues, erasedValues, erasedNullRestrictedValues, erasedNullableValues;
  {
    values = new Value[1024];
    nullableValues = (Value[]) ValueClass.newNullableAtomicArray(Value.class, 1024);
    nullRestrictedValues = (Value[]) ValueClass.newNullRestrictedAtomicArray(Value.class, 1024, new Value(0));
    genericValues = new Object[1024];
    for(var i = 0; i < 1024; i++) {
      var value = new Value(i);
      values[i] = value;
      nullableValues[i] = value;
      nullRestrictedValues[i] = value;
      genericValues[i] = value;
    }
    erasedValues = values;
    erasedNullableValues = nullableValues;
    erasedNullRestrictedValues = nullRestrictedValues;
  }

  value record Value(int v) {}

  //@Benchmark
  public int sumValues() {
    var sum = 0;
    for(var value : values) {
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  public int sumNullableValues() {
    var sum = 0;
    for(var value : nullableValues) {
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  public int sumNullRestrictedValues() {
    var sum = 0;
    for(var value : nullRestrictedValues) {
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  public int sumGenericValues() {
    var sum = 0;
    for(var o : genericValues) {
      var value = (Value) o;
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  public int sumErasedValues() {
    var sum = 0;
    for(var o : erasedValues) {
      var value = (Value) o;
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  public int sumErasedNullableValues() {
    var sum = 0;
    for(var o : erasedNullableValues) {
      var value = (Value) o;
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  public int sumErasedNullRestrictedValues() {
    var sum = 0;
    for(var o : erasedNullRestrictedValues) {
      var value = (Value) o;
      sum += value.v;
    }
    return sum;
  }
}