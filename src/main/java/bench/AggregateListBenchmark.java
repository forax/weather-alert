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
import util.AggregateGenerator;
import util.ValueList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"--enable-preview"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class AggregateListBenchmark {

  private value record Tuple(int left, int right) {}

  private static final class SpecializedList extends AbstractList<Tuple> {
    private final List<ValueList<Integer>> lists;

    SpecializedList(List<ValueList<Integer>> lists) {
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

  private static final AggregateGenerator.AggregateFactory<Tuple> FACTORY =
      AggregateGenerator.factory(MethodHandles.lookup(), Tuple.class);

  private final ValueList<Integer> list0 = new ValueList<>(Integer.class, 1024);
  private final ValueList<Integer> list1 = new ValueList<>(Integer.class, 1024);
  {
    for(var i = 0; i < 1024; i++) {
      list0.add(i);
      list1.add(i);
    }
  }

  private final SpecializedList specializedList = new SpecializedList(List.of(list0, list1));
  //private AggregateList<Tuple> aggregateList = new AggregateList<>(lists, Tuple.class);
  private final AggregateGenerator.AggregateList<Tuple> aggregateList = FACTORY.create(list0, list1);

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
  public int sumAggregateList() {
    var sum = 0;
    for (var i = 0; i < aggregateList.size(); i++) {
      var tuple = aggregateList.get(i);
      sum += tuple.left + tuple.right;
    }
    return sum;
  }
}