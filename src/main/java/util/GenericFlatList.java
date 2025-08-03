package util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.value.ValueClass;
import jdk.internal.misc.Unsafe;

public final class GenericFlatList<E> extends AbstractList<E> {
  private static final boolean VALUE_CLASS_AVAILABLE;
  private static final ClassValue<Object> DEFAULT_VALUE;
  static {
    boolean valueClassAvailable;
    try {
      var _ = ValueClass.class;  // check that ValueClass is visible
      valueClassAvailable = true;
    } catch (IllegalAccessError _) {
      valueClassAvailable = false;
      System.err.println("WARNING: flat arrays are not available !");
    }
    VALUE_CLASS_AVAILABLE = valueClassAvailable;

    ClassValue<Object> defaultValue;
    try {
      defaultValue = new ClassValue<>() {
        private static final Unsafe UNSAFE = Unsafe.getUnsafe();  // check that Unsafe is visible

        @Override
        protected Object computeValue(Class<?> type) {
          try {
            return UNSAFE.allocateInstance(type);
          } catch (InstantiationException e) {
            throw new AssertionError(e);
          }
        }
      };
    } catch (IllegalAccessError _) {
      defaultValue = null;
      System.err.println("WARNING: default value is not available !");
    }
    DEFAULT_VALUE = defaultValue;
  }

  private E[] values;
  private int size;

  @SuppressWarnings("unchecked")
  public GenericFlatList(Class<? extends E> elementType, int capacity) {
    if (VALUE_CLASS_AVAILABLE) {
      if (DEFAULT_VALUE != null) {
        values = (E[]) ValueClass.newNullRestrictedAtomicArray(elementType, capacity, DEFAULT_VALUE.get(elementType));
        return;
      }
      values = (E[]) ValueClass.newNullableAtomicArray(elementType, capacity);
      return;
    }
    values = (E[]) Array.newInstance(elementType, capacity);
  }

  public GenericFlatList(Class<? extends E> valueClass) {
    this(valueClass, 16);
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
    if (VALUE_CLASS_AVAILABLE) {
      if (DEFAULT_VALUE != null) {
        var componentType = values.getClass().getComponentType();
        var newArray = (E[]) ValueClass.newNullRestrictedAtomicArray(componentType, newCapacity, DEFAULT_VALUE.get(componentType));
        System.arraycopy(values, 0, newArray, 0, values.length);
        values = newArray;
        return;
      }
      values = (E[]) ValueClass.copyOfSpecialArray(values, 0, newCapacity);
      return;
    }
    values = Arrays.copyOf(values, newCapacity);
  }

  /*private void expandToNullableArray() {
    var valueClass = values.getClass().getComponentType();
    @SuppressWarnings("unchecked")
    var newArray = (E[]) ValueClass.newNullableAtomicArray(valueClass, values.length);
    System.arraycopy(values, 0, newArray, 0, size);
    values = newArray;
  }*/

  @Override
  public boolean add(E element) {
    if (size == values.length) {
      resize();
    }
    /*if (NULL_RESTRICTED_ARRAY_AVAILABLE && element == null && ValueClass.isNullRestrictedArray(values)) {
      expandToNullableArray();
    }*/
    values[size++] = element;
    return true;
  }
}
