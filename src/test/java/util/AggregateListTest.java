package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class AggregateListTest {

  @Test
  @DisplayName("Testing basic operations with TestRecord")
  public void testBasicOperations() {
    record TestRecord(int id, String name) {}
    // Create a factory for TestRecord
    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    // Create lists to aggregate
    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob", "Charlie");

    // Create the aggregate list
    var aggregateList = factory.create(ids, names);

    // Test size
    assertEquals(3, aggregateList.size());
    assertFalse(aggregateList.isEmpty());

    // Test get
    assertEquals(new TestRecord(1, "Alice"), aggregateList.get(0));
    assertEquals(new TestRecord(2, "Bob"), aggregateList.get(1));
    assertEquals(new TestRecord(3, "Charlie"), aggregateList.get(2));

    // Test indexOf and contains
    assertTrue(aggregateList.contains(new TestRecord(2, "Bob")));
    assertEquals(1, aggregateList.indexOf(new TestRecord(2, "Bob")));
    assertEquals(-1, aggregateList.indexOf(new TestRecord(4, "Dave")));

    // Test lastIndexOf
    assertEquals(2, aggregateList.lastIndexOf(new TestRecord(3, "Charlie")));
  }

  @Test
  @DisplayName("Testing basic operations with SimpleRecord")
  public void testBasicOperationsWithSimpleRecord() {
    record SimpleRecord(int id, String name) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), SimpleRecord.class);

    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob", "Charlie");

    var aggregateList = factory.create(ids, names);

    assertEquals(3, aggregateList.size());
    assertFalse(aggregateList.isEmpty());

    assertEquals(new SimpleRecord(1, "Alice"), aggregateList.get(0));
    assertEquals(new SimpleRecord(2, "Bob"), aggregateList.get(1));
    assertEquals(new SimpleRecord(3, "Charlie"), aggregateList.get(2));
  }

  @Test
  @DisplayName("Factory with single field record")
  public void testFactoryWithSingleFieldRecord() {
    record SingleValue(long value) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), SingleValue.class);

    var values = List.of(100L, 200L, 300L);

    var aggregateList = factory.create(values);

    assertEquals(3, aggregateList.size());
    assertEquals(new SingleValue(100L), aggregateList.get(0));
    assertEquals(new SingleValue(200L), aggregateList.get(1));
    assertEquals(new SingleValue(300L), aggregateList.get(2));
  }

  @Test
  @DisplayName("Factory with wide record having 8 fields")
  public void testFactoryWithWideRecord() {
    record WideRecord(int a, int b, int c, int d, int e, int f, int g, int h) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), WideRecord.class);

    var aValues = List.of(1, 2);
    var bValues = List.of(10, 20);
    var cValues = List.of(100, 200);
    var dValues = List.of(1000, 2000);
    var eValues = List.of(10000, 20000);
    var fValues = List.of(100000, 200000);
    var gValues = List.of(1000000, 2000000);
    var hValues = List.of(10000000, 20000000);

    var aggregateList =
        factory.create(aValues, bValues, cValues, dValues, eValues, fValues, gValues, hValues);

    assertEquals(2, aggregateList.size());
    assertEquals(
        new WideRecord(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000), aggregateList.get(0));
    assertEquals(
        new WideRecord(2, 20, 200, 2000, 20000, 200000, 2000000, 20000000), aggregateList.get(1));
  }

  @Test
  @DisplayName("Testing with byte component record")
  public void testWithByteComponentRecord() {
    record ByteRecord(byte value, String name) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), ByteRecord.class);

    var values = List.of((byte) 10, (byte) 20, (byte) 30);
    var names = List.of("Alpha", "Beta", "Gamma");

    var aggregateList = factory.create(values, names);

    assertEquals(3, aggregateList.size());
    assertEquals(new ByteRecord((byte) 10, "Alpha"), aggregateList.get(0));
    assertEquals(new ByteRecord((byte) 20, "Beta"), aggregateList.get(1));
    assertEquals(new ByteRecord((byte) 30, "Gamma"), aggregateList.get(2));

    // Test mutable operations
    var mutableValues = new ArrayList<>(values);
    var mutableNames = new ArrayList<>(names);
    var mutableList = factory.create(mutableValues, mutableNames);

    var oldValue = mutableList.set(1, new ByteRecord((byte) 25, "Delta"));
    assertEquals(new ByteRecord((byte) 20, "Beta"), oldValue);
    assertEquals(new ByteRecord((byte) 25, "Delta"), mutableList.get(1));

    assertEquals((byte) 25, mutableValues.get(1));
    assertEquals("Delta", mutableNames.get(1));
  }

  @Test
  @DisplayName("Testing with char component record")
  public void testWithCharComponentRecord() {
    record CharRecord(char symbol, int count) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), CharRecord.class);

    var symbols = List.of('A', 'B', 'C');
    var counts = List.of(5, 10, 15);

    var aggregateList = factory.create(symbols, counts);

    assertEquals(3, aggregateList.size());
    assertEquals(new CharRecord('A', 5), aggregateList.get(0));
    assertEquals(new CharRecord('B', 10), aggregateList.get(1));
    assertEquals(new CharRecord('C', 15), aggregateList.get(2));

    // Test contains and indexOf
    assertTrue(aggregateList.contains(new CharRecord('B', 10)));
    assertEquals(1, aggregateList.indexOf(new CharRecord('B', 10)));
    assertEquals(-1, aggregateList.indexOf(new CharRecord('D', 20)));
  }

  @Test
  @DisplayName("Testing with short component record")
  public void testWithShortComponentRecord() {
    record ShortRecord(short id, short value) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), ShortRecord.class);

    var ids = List.of((short) 100, (short) 200, (short) 300);
    var values = List.of((short) 1000, (short) 2000, (short) 3000);

    var aggregateList = factory.create(ids, values);

    assertEquals(3, aggregateList.size());
    assertEquals(new ShortRecord((short) 100, (short) 1000), aggregateList.get(0));
    assertEquals(new ShortRecord((short) 200, (short) 2000), aggregateList.get(1));
    assertEquals(new ShortRecord((short) 300, (short) 3000), aggregateList.get(2));

    // Test toArray
    var array = aggregateList.toArray();
    assertEquals(3, array.length);
    assertEquals(new ShortRecord((short) 100, (short) 1000), array[0]);

    var typedArray = new ShortRecord[3];
    var result = aggregateList.toArray(typedArray);
    assertSame(typedArray, result);
    assertEquals(new ShortRecord((short) 200, (short) 2000), result[1]);
  }

  @Test
  @DisplayName("Testing with mixed primitive component record")
  public void testWithMixedPrimitiveComponentRecord() {
    record MixedRecord(byte b, char c, short s) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), MixedRecord.class);

    var byteValues = List.of((byte) 1, (byte) 2, (byte) 3);
    var charValues = List.of('X', 'Y', 'Z');
    var shortValues = List.of((short) 100, (short) 200, (short) 300);

    var aggregateList = factory.create(byteValues, charValues, shortValues);

    assertEquals(3, aggregateList.size());
    assertEquals(new MixedRecord((byte) 1, 'X', (short) 100), aggregateList.get(0));
    assertEquals(new MixedRecord((byte) 2, 'Y', (short) 200), aggregateList.get(1));
    assertEquals(new MixedRecord((byte) 3, 'Z', (short) 300), aggregateList.get(2));

    // Test iterator
    var iterator = aggregateList.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(new MixedRecord((byte) 1, 'X', (short) 100), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(new MixedRecord((byte) 2, 'Y', (short) 200), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(new MixedRecord((byte) 3, 'Z', (short) 300), iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  @DisplayName("Testing with 5 component lists")
  public void testWithFiveComponentLists() {
    record FiveFieldRecord(int a, String b, double c, boolean d, char e) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), FiveFieldRecord.class);

    var aValues = List.of(1, 2, 3);
    var bValues = List.of("Alpha", "Beta", "Gamma");
    var cValues = List.of(1.1, 2.2, 3.3);
    var dValues = List.of(true, false, true);
    var eValues = List.of('A', 'B', 'C');

    var aggregateList = factory.create(aValues, bValues, cValues, dValues, eValues);

    assertEquals(3, aggregateList.size());
    assertEquals(new FiveFieldRecord(1, "Alpha", 1.1, true, 'A'), aggregateList.get(0));
    assertEquals(new FiveFieldRecord(2, "Beta", 2.2, false, 'B'), aggregateList.get(1));
    assertEquals(new FiveFieldRecord(3, "Gamma", 3.3, true, 'C'), aggregateList.get(2));

    // Test mutable operations if needed
    var mutableA = new ArrayList<>(aValues);
    var mutableB = new ArrayList<>(bValues);
    var mutableC = new ArrayList<>(cValues);
    var mutableD = new ArrayList<>(dValues);
    var mutableE = new ArrayList<>(eValues);

    var mutableList = factory.create(mutableA, mutableB, mutableC, mutableD, mutableE);

    var oldValue = mutableList.set(1, new FiveFieldRecord(20, "Beta2", 2.5, true, 'X'));
    assertEquals(new FiveFieldRecord(2, "Beta", 2.2, false, 'B'), oldValue);
    assertEquals(new FiveFieldRecord(20, "Beta2", 2.5, true, 'X'), mutableList.get(1));

    assertEquals(20, mutableA.get(1));
    assertEquals("Beta2", mutableB.get(1));
    assertEquals(2.5, mutableC.get(1));
    assertEquals(true, mutableD.get(1));
    assertEquals('X', mutableE.get(1));
  }

  @Test
  @DisplayName("Testing with 6 component lists")
  public void testWithSixComponentLists() {
    record SixFieldRecord(int a, String b, double c, boolean d, char e, long f) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), SixFieldRecord.class);

    var aValues = List.of(1, 2, 3);
    var bValues = List.of("Alpha", "Beta", "Gamma");
    var cValues = List.of(1.1, 2.2, 3.3);
    var dValues = List.of(true, false, true);
    var eValues = List.of('A', 'B', 'C');
    var fValues = List.of(100L, 200L, 300L);

    var aggregateList = factory.create(aValues, bValues, cValues, dValues, eValues, fValues);

    assertEquals(3, aggregateList.size());
    assertEquals(new SixFieldRecord(1, "Alpha", 1.1, true, 'A', 100L), aggregateList.get(0));
    assertEquals(new SixFieldRecord(2, "Beta", 2.2, false, 'B', 200L), aggregateList.get(1));
    assertEquals(new SixFieldRecord(3, "Gamma", 3.3, true, 'C', 300L), aggregateList.get(2));

    // Test mutable operations
    var mutableA = new ArrayList<>(aValues);
    var mutableB = new ArrayList<>(bValues);
    var mutableC = new ArrayList<>(cValues);
    var mutableD = new ArrayList<>(dValues);
    var mutableE = new ArrayList<>(eValues);
    var mutableF = new ArrayList<>(fValues);

    var mutableList = factory.create(mutableA, mutableB, mutableC, mutableD, mutableE, mutableF);

    var oldValue = mutableList.set(1, new SixFieldRecord(20, "Beta2", 2.5, true, 'X', 250L));
    assertEquals(new SixFieldRecord(2, "Beta", 2.2, false, 'B', 200L), oldValue);
    assertEquals(new SixFieldRecord(20, "Beta2", 2.5, true, 'X', 250L), mutableList.get(1));

    assertEquals(20, mutableA.get(1));
    assertEquals("Beta2", mutableB.get(1));
    assertEquals(2.5, mutableC.get(1));
    assertEquals(true, mutableD.get(1));
    assertEquals('X', mutableE.get(1));
    assertEquals(250L, mutableF.get(1));
  }

  @Test
  @DisplayName("Testing with 7 component lists")
  public void testWithSevenComponentLists() {
    record SevenFieldRecord(int a, String b, double c, boolean d, char e, long f, byte g) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), SevenFieldRecord.class);

    var aValues = List.of(1, 2, 3);
    var bValues = List.of("Alpha", "Beta", "Gamma");
    var cValues = List.of(1.1, 2.2, 3.3);
    var dValues = List.of(true, false, true);
    var eValues = List.of('A', 'B', 'C');
    var fValues = List.of(100L, 200L, 300L);
    var gValues = List.of((byte) 10, (byte) 20, (byte) 30);

    var aggregateList =
        factory.create(aValues, bValues, cValues, dValues, eValues, fValues, gValues);

    assertEquals(3, aggregateList.size());
    assertEquals(
        new SevenFieldRecord(1, "Alpha", 1.1, true, 'A', 100L, (byte) 10), aggregateList.get(0));
    assertEquals(
        new SevenFieldRecord(2, "Beta", 2.2, false, 'B', 200L, (byte) 20), aggregateList.get(1));
    assertEquals(
        new SevenFieldRecord(3, "Gamma", 3.3, true, 'C', 300L, (byte) 30), aggregateList.get(2));

    // Test indexOf and contains
    assertTrue(
        aggregateList.contains(new SevenFieldRecord(2, "Beta", 2.2, false, 'B', 200L, (byte) 20)));
    assertEquals(
        1,
        aggregateList.indexOf(new SevenFieldRecord(2, "Beta", 2.2, false, 'B', 200L, (byte) 20)));
    assertEquals(
        -1,
        aggregateList.indexOf(new SevenFieldRecord(4, "Delta", 4.4, true, 'D', 400L, (byte) 40)));

    // Test mutable operations
    var mutableA = new ArrayList<>(aValues);
    var mutableB = new ArrayList<>(bValues);
    var mutableC = new ArrayList<>(cValues);
    var mutableD = new ArrayList<>(dValues);
    var mutableE = new ArrayList<>(eValues);
    var mutableF = new ArrayList<>(fValues);
    var mutableG = new ArrayList<>(gValues);

    var mutableList =
        factory.create(mutableA, mutableB, mutableC, mutableD, mutableE, mutableF, mutableG);

    var oldValue =
        mutableList.set(1, new SevenFieldRecord(20, "Beta2", 2.5, true, 'X', 250L, (byte) 25));
    assertEquals(new SevenFieldRecord(2, "Beta", 2.2, false, 'B', 200L, (byte) 20), oldValue);
    assertEquals(new SevenFieldRecord(20, "Beta2", 2.5, true, 'X', 250L, (byte) 25), mutableList.get(1));

    assertEquals(20, mutableA.get(1));
    assertEquals("Beta2", mutableB.get(1));
    assertEquals(2.5, mutableC.get(1));
    assertEquals(true, mutableD.get(1));
    assertEquals('X', mutableE.get(1));
    assertEquals(250L, mutableF.get(1));
    assertEquals((byte) 25, mutableG.get(1));
  }

  @Test
  @DisplayName("Testing with nested record structure")
  public void testWithNestedRecordStructure() {
    record Inner(String data) {}
    record Outer(int id, Inner inner) {}

    // For nested records, we need to flatten the components
    var factory = AggregateList.factory(MethodHandles.lookup(), Outer.class);

    var ids = List.of(1, 2, 3);
    var inners = List.of(new Inner("first"), new Inner("second"), new Inner("third"));

    var aggregateList = factory.create(ids, inners);

    assertEquals(3, aggregateList.size());
    assertEquals(new Outer(1, new Inner("first")), aggregateList.get(0));
    assertEquals(new Outer(2, new Inner("second")), aggregateList.get(1));
    assertEquals(new Outer(3, new Inner("third")), aggregateList.get(2));
  }

  @Test
  @DisplayName("Testing with boolean field record")
  public void testWithBooleanFieldRecord() {
    record Flag(String name, boolean active) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), Flag.class);

    var names = List.of("Feature A", "Feature B", "Feature C");
    var statuses = List.of(true, false, true);

    var aggregateList = factory.create(names, statuses);

    assertEquals(3, aggregateList.size());
    assertEquals(new Flag("Feature A", true), aggregateList.get(0));
    assertEquals(new Flag("Feature B", false), aggregateList.get(1));
    assertEquals(new Flag("Feature C", true), aggregateList.get(2));

    // Test indexOf with boolean fields
    assertEquals(1, aggregateList.indexOf(new Flag("Feature B", false)));

    // Test setting values with boolean fields
    var mutableNames = new ArrayList<>(names);
    var mutableStatuses = new ArrayList<>(statuses);
    var mutableList = factory.create(mutableNames, mutableStatuses);

    mutableList.set(0, new Flag("Feature X", false));
    assertEquals("Feature X", mutableNames.getFirst());
    assertEquals(false, mutableStatuses.getFirst());
  }

  @Test
  @DisplayName("Testing with primitive array handling for float values")
  public void testWithPrimitiveArrayHandling() {
    record WeatherData(float temperature, float humidity, float pressure) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), WeatherData.class);

    var temperatures = List.of(22.5f, 23.1f, 21.8f);
    var humidities = List.of(65.0f, 70.2f, 68.5f);
    var pressures = List.of(1013.2f, 1012.8f, 1014.0f);

    var aggregateList = factory.create(temperatures, humidities, pressures);

    assertEquals(3, aggregateList.size());

    // Test primitive float equality with delta
    var firstRecord = aggregateList.getFirst();
    assertEquals(22.5f, firstRecord.temperature(), 0.001f);
    assertEquals(65.0f, firstRecord.humidity(), 0.001f);
    assertEquals(1013.2f, firstRecord.pressure(), 0.001f);

    // Test contains with float precision
    assertTrue(aggregateList.contains(new WeatherData(23.1f, 70.2f, 1012.8f)));
  }

  @Test
  @DisplayName("Testing with mutable lists")
  public void testMutableLists() {
    record TestRecord(int id, String name) {}
    // Create a factory for TestRecord
    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    // Create mutable lists to aggregate
    var ids = new ArrayList<>(List.of(1, 2, 3));
    var names = new ArrayList<>(List.of("Alice", "Bob", "Charlie"));

    // Create the aggregate list
    var aggregateList = factory.create(ids, names);

    // Test set operation
    var oldValue = aggregateList.set(1, new TestRecord(22, "Bobby"));
    assertEquals(new TestRecord(2, "Bob"), oldValue);
    assertEquals(new TestRecord(22, "Bobby"), aggregateList.get(1));

    // Verify underlying lists were modified
    assertEquals(22, ids.get(1));
    assertEquals("Bobby", names.get(1));
  }

  @Test
  @DisplayName("Testing mutable lists with Person record")
  public void testMutableListsWithPersonRecord() {
    record Person(String firstName, String lastName, int age) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), Person.class);

    var firstNames = new ArrayList<>(List.of("John", "Jane", "Bob"));
    var lastNames = new ArrayList<>(List.of("Doe", "Smith", "Johnson"));
    var ages = new ArrayList<>(List.of(30, 25, 40));

    var aggregateList = factory.create(firstNames, lastNames, ages);

    var oldValue = aggregateList.set(1, new Person("Janet", "Williams", 28));
    assertEquals(new Person("Jane", "Smith", 25), oldValue);
    assertEquals(new Person("Janet", "Williams", 28), aggregateList.get(1));

    assertEquals("Janet", firstNames.get(1));
    assertEquals("Williams", lastNames.get(1));
    assertEquals(28, ages.get(1));
  }

  @Test
  @DisplayName("Testing iterator functionality")
  public void testIterator() {
    record TestRecord(int id, String name) {}
    // Create a factory for TestRecord
    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    // Create lists to aggregate
    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob", "Charlie");

    // Create the aggregate list
    var aggregateList = factory.create(ids, names);

    // Test iterator
    var iterator = aggregateList.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(new TestRecord(1, "Alice"), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(new TestRecord(2, "Bob"), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(new TestRecord(3, "Charlie"), iterator.next());
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  @DisplayName("Testing iterator with Point record")
  public void testIteratorWithPointRecord() {
    record Point(double x, double y, double z) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), Point.class);

    var xCoords = List.of(1.0, 2.0, 3.0);
    var yCoords = List.of(4.0, 5.0, 6.0);
    var zCoords = List.of(7.0, 8.0, 9.0);

    var aggregateList = factory.create(xCoords, yCoords, zCoords);

    var iterator = aggregateList.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(new Point(1.0, 4.0, 7.0), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(new Point(2.0, 5.0, 8.0), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(new Point(3.0, 6.0, 9.0), iterator.next());
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  @DisplayName("Testing toArray methods")
  public void testToArray() {
    record TestRecord(int id, String name) {}
    // Create a factory for TestRecord
    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    // Create lists to aggregate
    var ids = List.of(1, 2);
    var names = List.of("Alice", "Bob");

    // Create the aggregate list
    var aggregateList = factory.create(ids, names);

    // Test toArray()
    var array = aggregateList.toArray();
    assertEquals(2, array.length);
    assertEquals(new TestRecord(1, "Alice"), array[0]);
    assertEquals(new TestRecord(2, "Bob"), array[1]);

    // Test toArray(T[])
    var typedArray = new TestRecord[2];
    var result = aggregateList.toArray(typedArray);
    assertSame(typedArray, result);
    assertEquals(new TestRecord(1, "Alice"), result[0]);
    assertEquals(new TestRecord(2, "Bob"), result[1]);
  }

  @Test
  @DisplayName("Testing toArray with Color record")
  public void testToArrayWithColorRecord() {
    record Color(int red, int green, int blue, int alpha) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), Color.class);

    var redValues = List.of(255, 0, 0, 128);
    var greenValues = List.of(0, 255, 0, 128);
    var blueValues = List.of(0, 0, 255, 128);
    var alphaValues = List.of(255, 255, 255, 128);

    var aggregateList = factory.create(redValues, greenValues, blueValues, alphaValues);

    var array = aggregateList.toArray();
    assertEquals(4, array.length);
    assertEquals(new Color(255, 0, 0, 255), array[0]);
    assertEquals(new Color(0, 255, 0, 255), array[1]);
    assertEquals(new Color(0, 0, 255, 255), array[2]);
    assertEquals(new Color(128, 128, 128, 128), array[3]);

    var typedArray = new Color[4];
    var result = aggregateList.toArray(typedArray);
    assertSame(typedArray, result);
    assertEquals(new Color(255, 0, 0, 255), result[0]);
  }

  @Test
  @DisplayName("Testing factory with different size lists")
  public void testFactoryWithDifferentSizeLists() {
    record TestRecord(int id, String name) {}
    // Create a factory for TestRecord
    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    // Create lists with different sizes
    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob");

    // Verify exception is thrown when creating with different size lists
    assertThrows(IllegalArgumentException.class, () -> factory.create(ids, names));
  }

  @Test
  @DisplayName("Testing that create() rejects null lists")
  public void testCreateWithNullLists() {
    record TestRecord(int id, String name) {}
    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    // Test with first parameter null
    assertThrows(NullPointerException.class, () -> factory.create(null, List.of("Alice", "Bob")));
    // Test with second parameter null
    assertThrows(NullPointerException.class, () -> factory.create(List.of(1, 2), null));
    // Test with both parameters null
    assertThrows(NullPointerException.class, () -> factory.create(null, null));
  }

  @Test
  @DisplayName("Testing that create() with a single list rejects null")
  public void testCreateSingleValueWithNullList() {
    record SingleValue(int value) {}
    var factory = AggregateList.factory(MethodHandles.lookup(), SingleValue.class);

    // Test with null list on single parameter
    assertThrows(NullPointerException.class, () -> factory.create(null));
  }


  @Test
  @DisplayName("Testing unsupported operations")
  public void testUnsupportedOperations() {
    record TestRecord(int id, String name) {}
    // Create a factory for TestRecord
    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    // Create lists to aggregate
    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob", "Charlie");

    // Create the aggregate list
    var aggregateList = factory.create(ids, names);

    // Test unsupported operations
    assertThrows(
        UnsupportedOperationException.class, () -> aggregateList.add(new TestRecord(4, "Dave")));
    assertThrows(UnsupportedOperationException.class, () -> aggregateList.remove(1));
    assertThrows(UnsupportedOperationException.class, aggregateList::clear);

    assertThrows(
        UnsupportedOperationException.class, () -> aggregateList.add(1, new TestRecord(4, "Dave")));
    assertThrows(
        UnsupportedOperationException.class,
        () -> aggregateList.addAll(List.of(new TestRecord(4, "Dave"), new TestRecord(5, "John"))));
    assertThrows(
        UnsupportedOperationException.class,
        () -> aggregateList.addAll(0, List.of(new TestRecord(4, "Dave"))));
    assertThrows(
        UnsupportedOperationException.class,
        () -> aggregateList.remove(new TestRecord(1, "Alice")));
    assertThrows(
        UnsupportedOperationException.class,
        () -> aggregateList.removeAll(List.of(new TestRecord(1, "Alice"))));
    assertThrows(
        UnsupportedOperationException.class,
        () -> aggregateList.retainAll(List.of(new TestRecord(1, "Alice"))));
    assertThrows(
        UnsupportedOperationException.class,
        () -> aggregateList.replaceAll(e -> new TestRecord(e.id() + 10, e.name())));
    assertThrows(
        UnsupportedOperationException.class, () -> aggregateList.sort((a, b) -> a.id() - b.id()));
  }

  @Test
  @DisplayName("Testing list with null values")
  public void testListWithNullValues() {
    record TestRecord(int id, String name) {}
    // Create a factory for TestRecord
    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    // Create lists with null values (for String, as int is primitive and can't be null)
    var ids = new ArrayList<>(List.of(1, 2, 3));
    var names = new ArrayList<>(Arrays.asList("Alice", null, "Charlie"));

    // Create the aggregate list
    var aggregateList = factory.create(ids, names);

    // Test null handling
    assertEquals(new TestRecord(2, null), aggregateList.get(1));
    assertTrue(aggregateList.contains(new TestRecord(2, null)));
    assertEquals(1, aggregateList.indexOf(new TestRecord(2, null)));
  }

  @Test
  @DisplayName("Testing equals and hashCode")
  public void testEqualsAndHashCode() {
    record TestRecord(int id, String name) {}

    var factory = AggregateList.factory(MethodHandles.lookup(), TestRecord.class);

    var ids1 = List.of(1, 2, 3);
    var names1 = List.of("A", "B", "C");
    var list1 = factory.create(ids1, names1);

    var ids2 = List.of(1, 2, 3);
    var names2 = List.of("A", "B", "C");
    var list2 = factory.create(ids2, names2);

    var ids3 = List.of(1, 2, 4); // Different value
    var names3 = List.of("A", "B", "C");
    var list3 = factory.create(ids3, names3);

    // Test equals
    assertEquals(list1, list2);
    assertNotEquals(list1, list3);

    // Test hashCode
    assertEquals(list1.hashCode(), list2.hashCode());
    assertNotEquals(list1.hashCode(), list3.hashCode());

    // Test toString
    assertTrue(list1.toString().contains("1"));
    assertTrue(list1.toString().contains("A"));
  }
}

