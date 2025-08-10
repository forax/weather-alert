package util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.value.ValueClass;
import jdk.internal.misc.Unsafe;

public final class GenericFlatList<E> extends AbstractList<E> {
  private static final ClassValue<Object> DEFAULT_VALUE = new ClassValue<>() {
    @Override
    protected Object computeValue(Class<?> type) {
      return DefaultValue.defaultValue(type);
    }
  };

  private static final class DefaultValue {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    private static Object defaultValue(Class<?> type) {
      try {
        return UNSAFE.allocateInstance(type);
      } catch (InstantiationException e) {
        throw new AssertionError(e);
      }
    }
  }

  public static final int NON_FLAT = 1;
  public static final int NON_ATOMIC = 2;
  public static final int NON_NULL = 4;
  public static final int FLAT = 0;
  private static final int NON_ATOMIC_NON_NULL = NON_ATOMIC | NON_NULL;

  private E[] values;
  private int size;

  private static void checkFlat(Object[] array) {
    if (!ValueClass.isFlatArray(array)) {
      throw new IllegalStateException("array is not a flat array");
    }
  }

  @SuppressWarnings("unchecked")
  public GenericFlatList(Class<? extends E> elementType, int properties, int capacity) {
    if (!elementType.isValue()) {
      throw new IllegalArgumentException("Element type must be a value type");
    }
    values = (E[]) switch (properties) {
      case FLAT -> {
        var values = ValueClass.newNullableAtomicArray(elementType, capacity);
        checkFlat(values);
        yield values;
      }
      case NON_FLAT -> Array.newInstance(elementType, capacity);
      case NON_NULL -> {
        var defaultValue = DEFAULT_VALUE.get(elementType);
        var values =  ValueClass.newNullRestrictedAtomicArray(elementType, capacity, defaultValue);
        checkFlat(values);
        yield values;
      }
      case NON_ATOMIC_NON_NULL -> {
        var defaultValue = DEFAULT_VALUE.get(elementType);
        var values =  ValueClass.newNullRestrictedNonAtomicArray(elementType, capacity, defaultValue);
        checkFlat(values);
        yield values;
      }
      default -> throw new IllegalArgumentException("Unknown properties: " + properties);
    };
  }

  public GenericFlatList(Class<? extends E> valueClass, int properties) {
    this(valueClass, properties, 16);
  }

  public GenericFlatList(Class<? extends E> valueClass) {
    this(valueClass, FLAT, 16);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public E get(int index) {
    Objects.checkIndex(index, size);
    return values[index];
  }

  @SuppressWarnings("unchecked")
  private void resize() {
    var newCapacity = Math.max(16, values.length << 1);
    if (!ValueClass.isNullRestrictedArray(values)) {
      values = Arrays.copyOf(values, newCapacity);
      return;
    }
    var componentType = values.getClass().getComponentType();
    var defaultValue = DEFAULT_VALUE.get(componentType);
    if (ValueClass.isAtomicArray(values)) {
      var newArray = ValueClass.newNullRestrictedAtomicArray(componentType, newCapacity, defaultValue);
      System.arraycopy(values, 0, newArray, 0, values.length);
      values = (E[]) newArray;
      return;
    }
    var newArray = ValueClass.newNullRestrictedNonAtomicArray(componentType, newCapacity, defaultValue);
    System.arraycopy(values, 0, newArray, 0, values.length);
    values = (E[]) newArray;
  }

  @Override
  public boolean add(E element) {
    if (size == values.length) {
      resize();
    }
    values[size++] = element;
    return true;
  }

  public boolean isFlat() {
    return ValueClass.isFlatArray(values);
  }
}
