package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public final class GenericAggregateListTest {

  @Test
  @DisplayName("Testing basic operations with TestRecord")
  public void testBasicOperations() {
    record TestRecord(int id, String name) {}
    
    // Create lists to aggregate
    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob", "Charlie");
    
    // Create the aggregate list
    var aggregateList = new GenericAggregateList<>(3,
        index -> new TestRecord(ids.get(index), names.get(index)));
    
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
    
    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob", "Charlie");
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new SimpleRecord(ids.get(index), names.get(index)));
    
    assertEquals(3, aggregateList.size());
    assertFalse(aggregateList.isEmpty());
    
    assertEquals(new SimpleRecord(1, "Alice"), aggregateList.get(0));
    assertEquals(new SimpleRecord(2, "Bob"), aggregateList.get(1));
    assertEquals(new SimpleRecord(3, "Charlie"), aggregateList.get(2));
  }
  
  @Test
  @DisplayName("Testing with single field record")
  public void testWithSingleFieldRecord() {
    record SingleValue(long value) {}
    
    var values = List.of(100L, 200L, 300L);
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new SingleValue(values.get(index)));
    
    assertEquals(3, aggregateList.size());
    assertEquals(new SingleValue(100L), aggregateList.get(0));
    assertEquals(new SingleValue(200L), aggregateList.get(1));
    assertEquals(new SingleValue(300L), aggregateList.get(2));
  }
  
  @Test
  @DisplayName("Testing with wide record having 8 fields")
  public void testWithWideRecord() {
    record WideRecord(int a, int b, int c, int d, int e, int f, int g, int h) {}
    
    var aValues = List.of(1, 2);
    var bValues = List.of(10, 20);
    var cValues = List.of(100, 200);
    var dValues = List.of(1000, 2000);
    var eValues = List.of(10000, 20000);
    var fValues = List.of(100000, 200000);
    var gValues = List.of(1000000, 2000000);
    var hValues = List.of(10000000, 20000000);
    
    var aggregateList = new GenericAggregateList<>(2,
        index -> new WideRecord(
            aValues.get(index), bValues.get(index), cValues.get(index), dValues.get(index),
            eValues.get(index), fValues.get(index), gValues.get(index), hValues.get(index)));
    
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
    
    var values = List.of((byte) 10, (byte) 20, (byte) 30);
    var names = List.of("Alpha", "Beta", "Gamma");
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new ByteRecord(values.get(index), names.get(index)));
    
    assertEquals(3, aggregateList.size());
    assertEquals(new ByteRecord((byte) 10, "Alpha"), aggregateList.get(0));
    assertEquals(new ByteRecord((byte) 20, "Beta"), aggregateList.get(1));
    assertEquals(new ByteRecord((byte) 30, "Gamma"), aggregateList.get(2));
  }
  
  @Test
  @DisplayName("Testing with char component record")
  public void testWithCharComponentRecord() {
    record CharRecord(char symbol, int count) {}
    
    var symbols = List.of('A', 'B', 'C');
    var counts = List.of(5, 10, 15);
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new CharRecord(symbols.get(index), counts.get(index)));
    
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
    
    var ids = List.of((short) 100, (short) 200, (short) 300);
    var values = List.of((short) 1000, (short) 2000, (short) 3000);
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new ShortRecord(ids.get(index), values.get(index)));
    
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
    
    var byteValues = List.of((byte) 1, (byte) 2, (byte) 3);
    var charValues = List.of('X', 'Y', 'Z');
    var shortValues = List.of((short) 100, (short) 200, (short) 300);
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new MixedRecord(byteValues.get(index), charValues.get(index), shortValues.get(index)));
    
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
  @DisplayName("Testing with 5 component fields")
  public void testWithFiveComponentFields() {
    record FiveFieldRecord(int a, String b, double c, boolean d, char e) {}
    
    var aValues = List.of(1, 2, 3);
    var bValues = List.of("Alpha", "Beta", "Gamma");
    var cValues = List.of(1.1, 2.2, 3.3);
    var dValues = List.of(true, false, true);
    var eValues = List.of('A', 'B', 'C');
    
    var aggregateList = new GenericAggregateList<>(3, index ->
        new FiveFieldRecord(
            aValues.get(index), 
            bValues.get(index), 
            cValues.get(index), 
            dValues.get(index), 
            eValues.get(index)));
    
    assertEquals(3, aggregateList.size());
    assertEquals(new FiveFieldRecord(1, "Alpha", 1.1, true, 'A'), aggregateList.get(0));
    assertEquals(new FiveFieldRecord(2, "Beta", 2.2, false, 'B'), aggregateList.get(1));
    assertEquals(new FiveFieldRecord(3, "Gamma", 3.3, true, 'C'), aggregateList.get(2));
  }
  
  @Test
  @DisplayName("Testing with nested record structure")
  public void testWithNestedRecordStructure() {
    record Inner(String data) {}
    record Outer(int id, Inner inner) {}
    
    var ids = List.of(1, 2, 3);
    var inners = List.of(new Inner("first"), new Inner("second"), new Inner("third"));
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new Outer(ids.get(index), inners.get(index)));
    
    assertEquals(3, aggregateList.size());
    assertEquals(new Outer(1, new Inner("first")), aggregateList.get(0));
    assertEquals(new Outer(2, new Inner("second")), aggregateList.get(1));
    assertEquals(new Outer(3, new Inner("third")), aggregateList.get(2));
  }
  
  @Test
  @DisplayName("Testing with boolean field record")
  public void testWithBooleanFieldRecord() {
    record Flag(String name, boolean active) {}
    
    var names = List.of("Feature A", "Feature B", "Feature C");
    var statuses = List.of(true, false, true);
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new Flag(names.get(index), statuses.get(index)));
    
    assertEquals(3, aggregateList.size());
    assertEquals(new Flag("Feature A", true), aggregateList.get(0));
    assertEquals(new Flag("Feature B", false), aggregateList.get(1));
    assertEquals(new Flag("Feature C", true), aggregateList.get(2));
    
    // Test indexOf with boolean fields
    assertEquals(1, aggregateList.indexOf(new Flag("Feature B", false)));
  }
  
  @Test
  @DisplayName("Testing with primitive array handling for float values")
  public void testWithPrimitiveArrayHandling() {
    record WeatherData(float temperature, float humidity, float pressure) {}
    
    var temperatures = List.of(22.5f, 23.1f, 21.8f);
    var humidities = List.of(65.0f, 70.2f, 68.5f);
    var pressures = List.of(1013.2f, 1012.8f, 1014.0f);
    
    var aggregateList = new GenericAggregateList<>(3, index ->
        new WeatherData(
            temperatures.get(index), 
            humidities.get(index), 
            pressures.get(index)));
    
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
  @DisplayName("Testing iterator functionality")
  public void testIterator() {
    record TestRecord(int id, String name) {}
    
    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob", "Charlie");
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new TestRecord(ids.get(index), names.get(index)));
    
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
  @DisplayName("Testing toArray methods")
  public void testToArray() {
    record TestRecord(int id, String name) {}
    
    var ids = List.of(1, 2);
    var names = List.of("Alice", "Bob");
    
    var aggregateList = new GenericAggregateList<>(2,
        index -> new TestRecord(ids.get(index), names.get(index)));
    
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
  @DisplayName("Testing list with null values")
  public void testListWithNullValues() {
    record TestRecord(int id, String name) {}
    
    var ids = new ArrayList<>(List.of(1, 2, 3));
    var names = new ArrayList<>(Arrays.asList("Alice", null, "Charlie"));
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new TestRecord(ids.get(index), names.get(index)));
    
    // Test null handling
    assertEquals(new TestRecord(2, null), aggregateList.get(1));
    assertTrue(aggregateList.contains(new TestRecord(2, null)));
    assertEquals(1, aggregateList.indexOf(new TestRecord(2, null)));
  }
  
  @Test
  @DisplayName("Testing equals and hashCode")
  public void testEqualsAndHashCode() {
    record TestRecord(int id, String name) {}
    
    var ids1 = List.of(1, 2, 3);
    var names1 = List.of("A", "B", "C");
    
    var list1 = new GenericAggregateList<>(3,
        index -> new TestRecord(ids1.get(index), names1.get(index)));
    
    var ids2 = List.of(1, 2, 3);
    var names2 = List.of("A", "B", "C");
    
    var list2 = new GenericAggregateList<>(3,
        index -> new TestRecord(ids2.get(index), names2.get(index)));
    
    var ids3 = List.of(1, 2, 4); // Different value
    var names3 = List.of("A", "B", "C");
    
    var list3 = new GenericAggregateList<>(3,
        index -> new TestRecord(ids3.get(index), names3.get(index)));
    
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
  
  @Test
  @DisplayName("Testing exception for invalid index")
  public void testInvalidIndex() {
    record TestRecord(int id, String name) {}
    
    var ids = List.of(1, 2, 3);
    var names = List.of("Alice", "Bob", "Charlie");
    
    var aggregateList = new GenericAggregateList<>(3,
        index -> new TestRecord(ids.get(index), names.get(index)));
    
    // Test exception for negative index
    assertThrows(IndexOutOfBoundsException.class, () -> aggregateList.get(-1));
    
    // Test exception for index >= size
    assertThrows(IndexOutOfBoundsException.class, () -> aggregateList.get(3));
  }
}