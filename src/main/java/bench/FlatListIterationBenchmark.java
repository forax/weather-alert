package bench;

import util.FlatListFactory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Benchmark                                                    (size)  Mode  Cnt      Score    Error  Units
// FlatListIterationBenchmark.sumArrayList                     10000  avgt    5   4529,930 ± 18,572  ns/op
// FlatListIterationBenchmark.sumFlatList                      10000  avgt    5  15152,822 ± 65,175  ns/op
// FlatListIterationBenchmark.sumNonFlatList                   10000  avgt    5   3308,946 ± 10,198  ns/op
// FlatListIterationBenchmark.sumNullRestrictedList            10000  avgt    5   2898,576 ± 14,536  ns/op
// FlatListIterationBenchmark.sumNullRestrictedNonAtomicList   10000  avgt    5   2898,853 ± 10,320  ns/op
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {
    "--enable-preview",
    "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
    "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class FlatListIterationBenchmark {

  @Param({/*"100", "1000",*/ "10000"})
  private int size;

  private List<Integer> arrayList;
  private List<Integer> nonFlatList;
  private List<Integer> flatList;
  private List<Integer> nullRestrictedList;
  private List<Integer> nonAtomicList;

  @Setup
  public void setup() {
    // Initialize both lists with the same data
    arrayList = new ArrayList<>(size);
    nonFlatList = FlatListFactory.create(Integer.class,
        FlatListFactory.NON_FLAT);
    flatList = FlatListFactory.create(Integer.class,
        FlatListFactory.FLAT);
    checkFlat(flatList);
    nullRestrictedList = FlatListFactory.create(Integer.class,
        FlatListFactory.NON_NULL_FLAT);
    checkFlat(nullRestrictedList);
    nonAtomicList = FlatListFactory.create(Integer.class,
        FlatListFactory.NON_ATOMIC_FLAT);
    checkFlat(nonAtomicList);

    for (var i = 0; i < size; i++) {
      var value = new Integer(i);
      arrayList.add(value);
      nonFlatList.add(value);
      flatList.add(value);
      nullRestrictedList.add(value);
      nonAtomicList.add(value);
    }
  }

  private static void checkFlat(List<?> list) {
    if (!FlatListFactory.isFlat(list)) {
      throw new AssertionError("list array is not a flat");
    }
  }

  //@Benchmark
  public int sumArrayList() {
    var sum = 0;
    for (var i = 0; i < arrayList.size(); i++) {
      var value = arrayList.get(i);
      sum += value;
    }
    return sum;
  }

  //@Benchmark
  public int sumNonFlatList() {
    var sum = 0;
    for (var i = 0; i < nonFlatList.size(); i++) {
      var value = nonFlatList.get(i);
      sum += value;
    }
    return sum;
  }

  //@Benchmark
  public int sumFlatList() {
    var sum = 0;
    for (var i = 0; i < flatList.size(); i++) {
      var value = flatList.get(i);
      sum += value;
    }
    return sum;
  }

  //@Benchmark
  public int sumNullRestrictedList() {
    var sum = 0;
    for (var i = 0; i < nullRestrictedList.size(); i++) {
      var value = nullRestrictedList.get(i);
      sum += value;
    }
    return sum;
  }

  //@Benchmark
  public int sumNonAtomicList() {
    var sum = 0;
    for (var i = 0; i < nonAtomicList.size(); i++) {
      var value = nonAtomicList.get(i);
      sum += value;
    }
    return sum;
  }
}