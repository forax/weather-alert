package util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;

public final class GenericValueListTest {

  // Test value record for testing purposes
  private value record TestValue(String data) {}

  // Regular record for negative testing
  private record RegularClass(String data) {}

  @Test
  @DisplayName("Should create empty ValueList with value class")
  public void testConstructorWithValueClass() {
    var list = new GenericValueList<>(TestValue.class);

    assertEquals(0, list.size());
    assertTrue(list.isEmpty());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException for non-value class")
  public void testConstructorWithNonValueClass() {
    assertThrows(IllegalArgumentException.class,
        () -> new GenericValueList<RegularClass>(RegularClass.class));
  }

  @Test
  @DisplayName("Should throw NullPointerException for null class")
  public void testConstructorWithNullClass() {
    assertThrows(NullPointerException.class,
        () -> new GenericValueList<>(null));
  }

  @Test
  @DisplayName("Should add single element and increase size")
  public void testAddSingleElement() {
    var list = new GenericValueList<>(TestValue.class);
    var element = new TestValue("test1");

    var result = list.add(element);

    Assertions.assertTrue(result);
    assertEquals(1, list.size());
    assertEquals(element, list.get(0));
    assertEquals(element, list.getFirst());
  }

  @Test
  @DisplayName("Should add multiple elements sequentially")
  public void testAddMultipleElements() {
    var list = new GenericValueList<>(TestValue.class);
    var element1 = new TestValue("test1");
    var element2 = new TestValue("test2");
    var element3 = new TestValue("test3");

    list.add(element1);
    list.add(element2);
    list.add(element3);

    assertEquals(3, list.size());
    assertEquals(element1, list.get(0));
    assertEquals(element2, list.get(1));
    assertEquals(element3, list.get(2));
  }

  @Test
  @Disabled
  @DisplayName("Should handle adding null elements")
  public void testAddNullElement() {
    var list = new GenericValueList<>(TestValue.class);

    var result = list.add(null);

    Assertions.assertTrue(result);
    assertEquals(1, list.size());
    assertNull(list.get(0));
  }

  @Test
  @DisplayName("Should expand capacity when needed")
  public void testCapacityExpansion() {
    var list = new GenericValueList<>(TestValue.class);

    // Add more than initial capacity to trigger expansion
    for (int i = 0; i < 20; i++) {
      list.add(new TestValue("test" + i));
    }

    assertEquals(20, list.size());
    for (int i = 0; i < 20; i++) {
      assertEquals("test" + i, list.get(i).data());
    }
  }

  @Test
  @DisplayName("Should get element at valid index")
  public void testGetValidIndex() {
    var list = new GenericValueList<>(TestValue.class);
    var element = new TestValue("test");
    list.add(element);

    var retrieved = list.get(0);

    assertEquals(element, retrieved);
  }

  @Test
  @DisplayName("Should throw IndexOutOfBoundsException for negative index")
  public void testGetNegativeIndex() {
    var list = new GenericValueList<>(TestValue.class);
    list.add(new TestValue("test"));

    assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
  }

  @Test
  @DisplayName("Should throw IndexOutOfBoundsException for index equal to size")
  public void testGetIndexEqualToSize() {
    var list = new GenericValueList<>(TestValue.class);
    list.add(new TestValue("test"));

    assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
  }

  @Test
  @DisplayName("Should throw IndexOutOfBoundsException for index greater than size")
  public void testGetIndexGreaterThanSize() {
    var list = new GenericValueList<>(TestValue.class);
    list.add(new TestValue("test"));

    assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
  }

  @Test
  @DisplayName("Should throw IndexOutOfBoundsException when accessing empty list")
  public void testGetFromEmptyList() {
    var list = new GenericValueList<>(TestValue.class);

    assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
  }

  @Test
  @DisplayName("Should return correct size after multiple operations")
  public void testSizeConsistency() {
    var list = new GenericValueList<>(TestValue.class);

    assertEquals(0, list.size());

    list.add(new TestValue("test1"));
    assertEquals(1, list.size());

    list.add(new TestValue("test2"));
    assertEquals(2, list.size());

    list.add(new TestValue("test3"));
    assertEquals(3, list.size());
  }

  @Test
  @DisplayName("Should handle list with 1 element correctly")
  public void testListSizeOne() {
    var list = new GenericValueList<>(TestValue.class);

    list.add(new TestValue("item0"));

    assertEquals(1, list.size());
    assertEquals("item0", list.get(0).data());
  }

  @Test
  @DisplayName("Should handle list with 5 elements correctly")
  public void testListSizeFive() {
    var list = new GenericValueList<>(TestValue.class);

    for (int i = 0; i < 5; i++) {
      list.add(new TestValue("item" + i));
    }

    assertEquals(5, list.size());

    for (int i = 0; i < 5; i++) {
      assertEquals("item" + i, list.get(i).data());
    }
  }

  @Test
  @DisplayName("Should handle list with 16 elements correctly")
  public void testListSizeSixteen() {
    var list = new GenericValueList<>(TestValue.class);

    for (int i = 0; i < 16; i++) {
      list.add(new TestValue("item" + i));
    }

    assertEquals(16, list.size());

    for (int i = 0; i < 16; i++) {
      assertEquals("item" + i, list.get(i).data());
    }
  }

  @Test
  @DisplayName("Should handle list with 17 elements correctly")
  public void testListSizeSeventeen() {
    var list = new GenericValueList<>(TestValue.class);

    for (int i = 0; i < 17; i++) {
      list.add(new TestValue("item" + i));
    }

    assertEquals(17, list.size());

    for (int i = 0; i < 17; i++) {
      assertEquals("item" + i, list.get(i).data());
    }
  }

  @Test
  @DisplayName("Should handle list with 32 elements correctly")
  public void testListSizeThirtyTwo() {
    var list = new GenericValueList<>(TestValue.class);

    for (int i = 0; i < 32; i++) {
      list.add(new TestValue("item" + i));
    }

    assertEquals(32, list.size());

    for (int i = 0; i < 32; i++) {
      assertEquals("item" + i, list.get(i).data());
    }
  }

  @Test
  @DisplayName("Should handle list with 100 elements correctly")
  public void testListSizeOneHundred() {
    var list = new GenericValueList<>(TestValue.class);

    for (int i = 0; i < 100; i++) {
      list.add(new TestValue("item" + i));
    }

    assertEquals(100, list.size());

    for (int i = 0; i < 100; i++) {
      assertEquals("item" + i, list.get(i).data());
    }
  }

  @Test
  @DisplayName("Should maintain order of added elements")
  public void testElementOrder() {
    var list = new GenericValueList<>(TestValue.class);
    var elements = new TestValue[] {
        new TestValue("first"),
        new TestValue("second"),
        new TestValue("third"),
        new TestValue("fourth")
    };

    for (var element : elements) {
      list.add(element);
    }

    for (int i = 0; i < elements.length; i++) {
      assertEquals(elements[i], list.get(i));
    }
  }

  @Test
  @DisplayName("Should work with inherited List methods")
  public void testInheritedListMethods() {
    var list = new GenericValueList<>(TestValue.class);
    var element1 = new TestValue("test1");
    var element2 = new TestValue("test2");

    list.add(element1);
    list.add(element2);

    assertTrue(list.contains(element1));
    assertTrue(list.contains(element2));
    assertFalse(list.contains(new TestValue("nonexistent")));

    assertEquals(0, list.indexOf(element1));
    assertEquals(1, list.indexOf(element2));
    assertEquals(-1, list.indexOf(new TestValue("nonexistent")));

    assertFalse(list.isEmpty());
  }

  @Test
  @DisplayName("Should handle edge case of exactly 16 elements")
  public void testExactly16Elements() {
    var list = new GenericValueList<>(TestValue.class);

    // Add exactly 16 elements (initial expansion threshold)
    for (int i = 0; i < 16; i++) {
      list.add(new TestValue("test" + i));
    }

    assertEquals(16, list.size());

    // Add one more to trigger expansion
    list.add(new TestValue("test16"));
    assertEquals(17, list.size());

    // Verify all elements are accessible
    for (int i = 0; i <= 16; i++) {
      assertEquals("test" + i, list.get(i).data());
    }
  }

  @Test
  @DisplayName("Should support iterator from AbstractList")
  public void testIterator() {
    var list = new GenericValueList<>(TestValue.class);
    var element1 = new TestValue("test1");
    var element2 = new TestValue("test2");

    list.add(element1);
    list.add(element2);

    var iterator = list.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(element1, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(element2, iterator.next());
    assertFalse(iterator.hasNext());

    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  @DisplayName("Should support enhanced for loop")
  public void testEnhancedForLoop() {
    var list = new GenericValueList<>(TestValue.class);
    var expectedData = new String[]{"first", "second", "third"};

    for (var data : expectedData) {
      list.add(new TestValue(data));
    }

    var index = 0;
    for (var element : list) {
      assertEquals(expectedData[index], element.data());
      index++;
    }
    assertEquals(expectedData.length, index);
  }

  @Test
  @DisplayName("Should handle large number of elements efficiently")
  public void testLargeScaleOperations() {
    var list = new GenericValueList<>(TestValue.class);
    
    for (int i = 0; i < 1_000_000; i++) {
        list.add(new TestValue("element" + i));
    }
    
    assertEquals(1_000_000, list.size());
    
    // Verify random access works correctly
    assertEquals("element0", list.get(0).data());
    assertEquals("element500000", list.get(500_000).data());
    assertEquals("element999999", list.get(999_999).data());
  }

  @Test
  @DisplayName("Should handle empty value class")
  public void testEmptyValueClass() {
    value record EmptyValue() {}
    
    var emptyValueList = new GenericValueList<>(EmptyValue.class);
    emptyValueList.add(new EmptyValue());
    
    assertEquals(1, emptyValueList.size());
    assertEquals(new EmptyValue(), emptyValueList.getFirst());
  }

  @Test
  @DisplayName("Should handle a value class with several fields")
  public void testSeveralFieldsValueClass() {
    value record CompositeValue(String name, int id, boolean flag) {}

    var compositeList = new GenericValueList<>(CompositeValue.class);
    compositeList.add(new CompositeValue("test", 42, true));

    assertEquals(1, compositeList.size());
    assertEquals(new CompositeValue("test", 42, true), compositeList.getFirst());
  }

  @Test
  @DisplayName("Should handle a value class inside a value class")
  public void testSeveralValueClasses() {
    value record ByteValue(byte val1) {}
    value record TwoByteValue(ByteValue v1, ByteValue v2) {}

    var twoByteList = new GenericValueList<>(TwoByteValue.class);
    twoByteList.add(new TwoByteValue(new ByteValue((byte) 1), new ByteValue((byte) 2)));

    assertEquals(1, twoByteList.size());
    assertEquals(new TwoByteValue(new ByteValue((byte) 1), new ByteValue((byte) 2)), twoByteList.getFirst());
  }
}