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

//Benchmark                                     Mode  Cnt    Score   Error  Units
//ArrayBenchmark.sumErasedNullRestrictedPoints  avgt    5  301,797 ± 1,084  ns/op
//ArrayBenchmark.sumErasedNullablePoints        avgt    5  319,418 ± 0,621  ns/op
//ArrayBenchmark.sumErasedPoints                avgt    5  319,303 ± 1,399  ns/op
//ArrayBenchmark.sumGenericPoints               avgt    5  413,781 ± 1,423  ns/op
//ArrayBenchmark.sumNullRestrictedPoints        avgt    5  302,071 ± 0,763  ns/op
//ArrayBenchmark.sumNullablePoints              avgt    5  319,629 ± 1,926  ns/op
//ArrayBenchmark.sumPoints                      avgt    5  319,342 ± 0,704  ns/op

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {
    "--enable-preview",
    "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED",
    "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ArrayComplexBenchmark {

  private Point[] points, nullRestrictedPoints, nullablePoints;
  private Object[] genericPoints, erasedPoints, erasedNullRestrictedPoints, erasedNullablePoints;
  {
    points = new Point[1024];
    nullablePoints = (Point[]) ValueClass.newNullableAtomicArray(Point.class, 1024);
    nullRestrictedPoints = (Point[]) ValueClass.newNullRestrictedAtomicArray(Point.class, 1024, new Point(0,0));
    genericPoints = new Object[1024];
    for(var i = 0; i < 1024; i++) {
      var point = new Point(i, i);
      points[i] = point;
      nullablePoints[i] = point;
      nullRestrictedPoints[i] = point;
      genericPoints[i] = point;
    }
    erasedPoints = points;
    erasedNullablePoints = nullablePoints;
    erasedNullRestrictedPoints = nullRestrictedPoints;
  }

  value record Point(int x, int y) {}

  //@Benchmark
  public int sumPoints() {
    var sum = 0;
    for(var point : nullablePoints) {
      sum += point.x + point.y;
    }
    return sum;
  }

  //@Benchmark
  public int sumNullablePoints() {
    var sum = 0;
    for(var point : nullablePoints) {
      sum += point.x + point.y;
    }
    return sum;
  }

  //@Benchmark
  public int sumNullRestrictedPoints() {
    var sum = 0;
    for(var point : nullRestrictedPoints) {
      sum += point.x + point.y;
    }
    return sum;
  }

  //@Benchmark
  public int sumGenericPoints() {
    var sum = 0;
    for(var o : genericPoints) {
      var point = (Point) o;
      sum += point.x + point.y;
    }
    return sum;
  }

  //@Benchmark
  public int sumErasedPoints() {
    var sum = 0;
    for(var o : erasedPoints) {
      var point = (Point) o;
      sum += point.x + point.y;
    }
    return sum;
  }

  //@Benchmark
  public int sumErasedNullablePoints() {
    var sum = 0;
    for(var o : erasedNullablePoints) {
      var point = (Point) o;
      sum += point.x + point.y;
    }
    return sum;
  }

  //@Benchmark
  public int sumErasedNullRestrictedPoints() {
    var sum = 0;
    for(var o : erasedNullRestrictedPoints) {
      var point = (Point) o;
      sum += point.x + point.y;
    }
    return sum;
  }
}