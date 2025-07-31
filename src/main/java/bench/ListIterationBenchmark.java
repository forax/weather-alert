package bench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import util.GenericValueList;
import util.ValueList;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

// Benchmark                                                 (size)  Mode  Cnt      Score    Error  Units
// ListIterationBenchmark.sumArrayList                        10000  avgt    5   4199,513 ± 14,517  ns/op
// ListIterationBenchmark.sumGenericValueList                 10000  avgt    5   3161,144 ± 10,539  ns/op
// ListIterationBenchmark.sumNullRestrictedGenericValueList   10000  avgt    5   2895,744 ± 12,455  ns/op
// ListIterationBenchmark.sumNullRestrictedValueList          10000  avgt    5   2895,486 ±  9,538  ns/op
// ListIterationBenchmark.sumNullableValueList                10000  avgt    5  13325,575 ± 54,399  ns/op
// ListIterationBenchmark.sumValueList                        10000  avgt    5   3138,913 ± 34,849  ns/op

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"--enable-preview"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ListIterationBenchmark {

  @Param({/*"100", "1000",*/ "10000"})
  private int size;

  private ArrayList<TestValue> arrayList;
  private GenericValueList<TestValue> genericValueList;
  private ValueList<TestValue> valueList;

  // Define a simple value class for testing
  value record TestValue(int v) {}

  @Setup
  public void setup() {
    // Initialize both lists with the same data
    arrayList = new ArrayList<>(size);
    genericValueList = new GenericValueList<>(TestValue.class, size);
    valueList = ValueList.create(TestValue.class, size);

    for (var i = 0; i < size; i++) {
      var value = new TestValue(i);
      arrayList.add(value);
      genericValueList.add(value);
      valueList.add(value);
    }
  }

  //@Benchmark
  public int sumArrayList() {
    var sum = 0;
    for (var i = 0; i < arrayList.size(); i++) {
      var value = arrayList.get(i);
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  public int sumGenericValueList() {
    var sum = 0;
    for (var i = 0; i < genericValueList.size(); i++) {
      var value = genericValueList.get(i);
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  @Fork(value = 1, jvmArgs = {
      "--enable-preview",
      "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
      "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public int sumNullRestrictedGenericValueList() {
    var sum = 0;
    for (var i = 0; i < genericValueList.size(); i++) {
      var value = genericValueList.get(i);
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  public int sumValueList() {
    var sum = 0;
    for (var i = 0; i < valueList.size(); i++) {
      var value = valueList.get(i);
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  @Fork(value = 1, jvmArgs = {
      "--enable-preview",
      "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED"})
  public int sumNullableValueList() {
    var sum = 0;
    for (var i = 0; i < valueList.size(); i++) {
      var value = valueList.get(i);
      sum += value.v;
    }
    return sum;
  }

  //@Benchmark
  @Fork(value = 1, jvmArgs = {
      "--enable-preview",
      "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
      "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public int sumNullRestrictedValueList() {
    var sum = 0;
    for (var i = 0; i < valueList.size(); i++) {
      var value = valueList.get(i);
      sum += value.v;
    }
    return sum;
  }
}