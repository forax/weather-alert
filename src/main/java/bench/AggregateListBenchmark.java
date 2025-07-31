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

import util.AggregateGenericList;
import util.AggregateList;
import util.GenericValueList;

import java.lang.invoke.MethodHandles;
import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Benchmark                                       Mode  Cnt    Score   Error  Units
// AggregateListBenchmark.sumAggregateGenericList  avgt    5  109,139 ± 1,189  ns/op
// AggregateListBenchmark.sumAggregateList         avgt    5  112,068 ± 2,655  ns/op
// AggregateListBenchmark.sumSpecializedList       avgt    5  112,199 ± 0,325  ns/op

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class AggregateListBenchmark {

  private value record Tuple(int left, int right) {}

  private static final class SpecializedList extends AbstractList<Tuple> {
    private final List<GenericValueList<Integer>> lists;

    SpecializedList(List<GenericValueList<Integer>> lists) {
      this.lists = lists;
    }

    @Override
    public int size() {
      return lists.getFirst().size();
    }

    @Override
    public Tuple get(int index) {
      return new Tuple(lists.get(0).get(index), lists.get(1).get(index));
    }
  }

  private final GenericValueList<Integer> list0 = new GenericValueList<>(Integer.class, 1024);
  private final GenericValueList<Integer> list1 = new GenericValueList<>(Integer.class, 1024);
  {
    for(var i = 0; i < 1024; i++) {
      list0.add(i);
      list1.add(i);
    }
  }

  private final SpecializedList specializedList = new SpecializedList(List.of(list0, list1));

  private static final AggregateList.Factory<Tuple> FACTORY =
      AggregateList.factory(MethodHandles.lookup(), Tuple.class);
  private final AggregateGenericList<Tuple> aggregateGenericList = new AggregateGenericList<>(list0.size(),
      i -> new Tuple(list0.get(i), list1.get(i)));
  private final AggregateList<Tuple> aggregateList = FACTORY.create(list0, list1);

  @Benchmark
  public int sumSpecializedList() {
    var sum = 0;
    for (var i = 0; i < specializedList.size(); i++) {
      var tuple = specializedList.get(i);
      sum += tuple.left + tuple.right;
    }
    return sum;
  }

  @Benchmark
  public int sumAggregateGenericList() {
    var sum = 0;
    for (var i = 0; i < aggregateGenericList.size(); i++) {
      var tuple = aggregateGenericList.get(i);
      sum += tuple.left + tuple.right;
    }
    return sum;
  }

  @Benchmark
  public int sumAggregateList() {
    var sum = 0;
    for (var i = 0; i < aggregateList.size(); i++) {
      var tuple = aggregateList.get(i);
      sum += tuple.left + tuple.right;
    }
    return sum;
  }
}