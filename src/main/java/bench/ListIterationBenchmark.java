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
import util.FlatListFactory;
import util.GenericFlatList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Benchmark                                         (size)  Mode  Cnt      Score    Error  Units
// ListIterationBenchmark.sumArrayList                10000  avgt    5   4476,397 ± 43,409  ns/op
// ListIterationBenchmark.sumFlatList                 10000  avgt    5  15148,477 ± 84,144  ns/op
// ListIterationBenchmark.sumGenericFlatList          10000  avgt    5  13278,837 ± 74,107  ns/op
// ListIterationBenchmark.sumGenericNonFlatList       10000  avgt    5   3306,593 ± 23,874  ns/op
// ListIterationBenchmark.sumGenericNonNullFlatList   10000  avgt    5   2894,967 ± 15,166  ns/op
// ListIterationBenchmark.sumNonFlatList              10000  avgt    5   3325,936 ±  9,111  ns/op
// ListIterationBenchmark.sumNonNullFlatList          10000  avgt    5   3325,824 ±  9,215  ns/op

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {
    "--enable-preview",
    "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
    "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ListIterationBenchmark {

  @Param({/*"100", "1000",*/ "10000"})
  private int size;

  private List<TestValue> arrayList;
  private List<TestValue> genericNonFlatList, genericFlatList, genericNonNullFlatList;
  private List<TestValue> nonFlatList, flatList, nonNonNullFlatList;

  // Define a simple value class for testing
  value record TestValue(int v) {}

  @Setup
  public void setup() {
    // Initialize both lists with the same data
    arrayList = new ArrayList<>(size);

    genericNonFlatList = new GenericFlatList<>(TestValue.class, GenericFlatList.NON_FLAT, size);
    genericFlatList = new GenericFlatList<>(TestValue.class, GenericFlatList.FLAT, size);
    genericNonNullFlatList =  new GenericFlatList<>(TestValue.class, GenericFlatList.NON_NULL, size);

    nonFlatList = FlatListFactory.create(TestValue.class, FlatListFactory.NON_FLAT);
    flatList = FlatListFactory.create(TestValue.class, FlatListFactory.FLAT);
    checkFlat(flatList);
    nonNonNullFlatList = FlatListFactory.create(TestValue.class, FlatListFactory.NON_NULL);
    checkFlat(nonNonNullFlatList);

    for (var i = 0; i < size; i++) {
      var value = new TestValue(i);
      arrayList.add(value);

      genericNonFlatList.add(value);
      genericFlatList.add(value);
      genericNonNullFlatList.add(value);

      nonFlatList.add(value);
      flatList.add(value);
      nonNonNullFlatList.add(value);
    }
  }

  private static void checkFlat(List<?> list) {
    if (!FlatListFactory.isFlat(list)) {
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
  public int sumGenericNonFlatList() {
    var sum = 0;
    for (var i = 0; i < genericNonFlatList.size(); i++) {
      var value = genericNonFlatList.get(i);
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
  public int sumGenericNonNullFlatList() {
    var sum = 0;
    for (var i = 0; i < genericNonNullFlatList.size(); i++) {
      var value = genericNonNullFlatList.get(i);
      sum += value.v;
    }
    return sum;
  }

  @Benchmark
  public int sumNonFlatList() {
    var sum = 0;
    for (var i = 0; i < nonFlatList.size(); i++) {
      var value = nonFlatList.get(i);
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
  public int sumNonNullFlatList() {
    var sum = 0;
    for (var i = 0; i < nonFlatList.size(); i++) {
      var value = nonFlatList.get(i);
      sum += value.v;
    }
    return sum;
  }
}