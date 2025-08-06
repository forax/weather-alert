package bench;

import jdk.internal.value.ValueClass;
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
import util.GenericFlatList;
import util.FlatList;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

// Benchmark                                                (size)  Mode  Cnt      Score    Error  Units
// ListIterationBenchmark.sumArrayList                       10000  avgt    5   4199,513 ± 14,517  ns/op
// ListIterationBenchmark.sumGenericFlatList                 10000  avgt    5   3161,144 ± 10,539  ns/op
// ListIterationBenchmark.sumNullRestrictedGenericFlatList   10000  avgt    5   2895,744 ± 12,455  ns/op
// ListIterationBenchmark.sumNullRestrictedFlatList          10000  avgt    5   2895,486 ±  9,538  ns/op
// ListIterationBenchmark.sumNullableFlatList                10000  avgt    5  13325,575 ± 54,399  ns/op
// ListIterationBenchmark.sumVFlatList                       10000  avgt    5   3138,913 ± 34,849  ns/op

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
  private GenericFlatList<TestValue> genericFlatList;
  private FlatList<TestValue> flatList;

  // Define a simple value class for testing
  value record TestValue(int v) {}

  @Setup
  public void setup() {
    // Initialize both lists with the same data
    arrayList = new ArrayList<>(size);
    genericFlatList = new GenericFlatList<>(TestValue.class, size);
    flatList = FlatList.create(TestValue.class, size);
    checkFlatIfAvailable(flatList);

    for (var i = 0; i < size; i++) {
      var value = new TestValue(i);
      arrayList.add(value);
      genericFlatList.add(value);
      flatList.add(value);
    }
  }

  private static void checkFlatIfAvailable(FlatList<?> flatList) {
    try {
      var _ = ValueClass.class;
    } catch(IllegalAccessError _) {
      return;  // okay !
    }
    if (!flatList.isFlat()) {
      throw new AssertionError("list array is not a flat");
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
  public int sumGenericFlatList() {
    var sum = 0;
    for (var i = 0; i < genericFlatList.size(); i++) {
      var value = genericFlatList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
  @Fork(value = 1, jvmArgs = {
      "--enable-preview",
      "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED"})
  public int sumNullableGenericFlatList() {
    var sum = 0;
    for (var i = 0; i < genericFlatList.size(); i++) {
      var value = genericFlatList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
  @Fork(value = 1, jvmArgs = {
      "--enable-preview",
      "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
      "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public int sumNullRestrictedGenericFlatList() {
    var sum = 0;
    for (var i = 0; i < genericFlatList.size(); i++) {
      var value = genericFlatList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
  public int sumFlatList() {
    var sum = 0;
    for (var i = 0; i < flatList.size(); i++) {
      var value = flatList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
  @Fork(value = 1, jvmArgs = {
      "--enable-preview",
      "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED"})
  public int sumNullableFlatList() {
    var sum = 0;
    for (var i = 0; i < flatList.size(); i++) {
      var value = flatList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
  @Fork(value = 1, jvmArgs = {
      "--enable-preview",
      "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
      "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
  public int sumNullRestrictedFlatList() {
    var sum = 0;
    for (var i = 0; i < flatList.size(); i++) {
      var value = flatList.get(i);
      sum += value.v;
    }
    return sum;
  }
}