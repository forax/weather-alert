package util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.value.ValueClass;
import jdk.internal.misc.Unsafe;

public final class GenericValueList<E> extends AbstractList<E> {
  private static final boolean NULL_RESTRICTED_ARRAY_AVAILABLE;
  private static final ClassValue<Object> DEFAULT_VALUE;
  static {
    boolean nullRestrictedArrayAvailable;
    ClassValue<Object> defaultValue;
    try {
      var _ = ValueClass.class;  // check that ValueClass is visible
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
      nullRestrictedArrayAvailable = true;
    } catch (IllegalAccessError _) {
      defaultValue = null;
      nullRestrictedArrayAvailable = false;
      System.err.println("WARNING: null restricted array not available !");
    }
    DEFAULT_VALUE = defaultValue;
    NULL_RESTRICTED_ARRAY_AVAILABLE = nullRestrictedArrayAvailable;
  }

  private E[] values;
  private int size;

  @SuppressWarnings("unchecked")
  public GenericValueList(Class<? extends E> valueClass, int initialCapacity) {
    if (!valueClass.isValue()) {
      throw new IllegalArgumentException("must be a value class");
    }
    if (NULL_RESTRICTED_ARRAY_AVAILABLE) {
      var defaultValue = DEFAULT_VALUE.get(valueClass);
      this.values = (E[]) ValueClass.newNullRestrictedAtomicArray(valueClass, initialCapacity, defaultValue);
      return;
    }
    this.values = (E[]) Array.newInstance(valueClass, initialCapacity);
    //this.values = (E[]) ValueClass.newNullableAtomicArray(valueClass, initialCapacity);
    //System.err.println("Value array " + valueClass.getName() + " " + ValueClass.isFlatArray(this.values));
  }

  public GenericValueList(Class<? extends E> valueClass) {
    this(valueClass, 0);
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

  private void resize() {
    var newSize = Math.max(16, values.length << 1);
    if (NULL_RESTRICTED_ARRAY_AVAILABLE && ValueClass.isNullRestrictedArray(values)) {
      var valueClass = values.getClass().getComponentType();
      var defaultValue = DEFAULT_VALUE.get(valueClass);
      @SuppressWarnings("unchecked")
      var newArray = (E[]) ValueClass.newNullRestrictedAtomicArray(valueClass, newSize, defaultValue);
      System.arraycopy(values, 0, newArray, 0, size);
      values = newArray;
    } else {
      // Arrays.copyOf only supports nullable arrays
      values = Arrays.copyOf(values, newSize);
    }
  }

  private void expandToNullableArray() {
    var valueClass = values.getClass().getComponentType();
    @SuppressWarnings("unchecked")
    var newArray = (E[]) ValueClass.newNullableAtomicArray(valueClass, values.length);
    System.arraycopy(values, 0, newArray, 0, size);
    values = newArray;
  }

  @Override
  public boolean add(E element) {
    if (size == values.length) {
      resize();
    }
    if (NULL_RESTRICTED_ARRAY_AVAILABLE && element == null && ValueClass.isNullRestrictedArray(values)) {
      expandToNullableArray();
    }
    values[size++] = element;
    return true;
  }
}
