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

//Benchmark                                                         (size)  Mode  Cnt     Score     Error  Units
//ListIterationBenchmark.sumArrayList                                  100  avgt    5    41,148 ±   0,351  ns/op
//ListIterationBenchmark.sumArrayList                                 1000  avgt    5   336,467 ±   4,209  ns/op
//ListIterationBenchmark.sumArrayList                                10000  avgt    5  4244,339 ±  17,953  ns/op
//ListIterationBenchmark.sumNullRestrictedValueList                    100  avgt    5    16,852 ±   0,097  ns/op
//ListIterationBenchmark.sumNullRestrictedValueList                   1000  avgt    5   272,608 ±   1,571  ns/op
//ListIterationBenchmark.sumNullRestrictedValueList                  10000  avgt    5  2897,307 ±  13,594  ns/op
//ListIterationBenchmark.sumValueList                                  100  avgt    5    25,605 ±   0,125  ns/op
//ListIterationBenchmark.sumValueList                                 1000  avgt    5   300,475 ±   1,230  ns/op
//ListIterationBenchmark.sumValueList                                10000  avgt    5  3160,731 ±   6,543  ns/op

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

  @Benchmark
  public int sumArrayList() {
    var sum = 0;
    for (var i = 0; i < arrayList.size(); i++) {
      var value = arrayList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
  public int sumGenericValueList() {
    var sum = 0;
    for (var i = 0; i < genericValueList.size(); i++) {
      var value = genericValueList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
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

  @Benchmark
  public int sumValueList() {
    var sum = 0;
    for (var i = 0; i < valueList.size(); i++) {
      var value = valueList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
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

  @Benchmark
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