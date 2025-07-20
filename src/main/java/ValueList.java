import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.value.ValueClass;
import jdk.internal.misc.Unsafe;

final class ValueList<E> extends AbstractList<E> {
  private static final boolean NULL_RESTRICTED_ARRAY_AVAILABLE;
  private static final ClassValue<Object> DEFAULT_VALUE;
  static {
    boolean nullRestrictedArrayAvailable;
    ClassValue<Object> defaultValue;
    try {
      var _ = ValueClass.class;  // check that ValueClass is visible
      defaultValue = new ClassValue<>() {
        private static final Unsafe UNSAFE = Unsafe.getUnsafe();

        @Override
        protected Object computeValue(Class<?> type) {
          if (!type.isValue()) {
            return defaultIdentityValue(type);
          }
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

  private static Object defaultIdentityValue(Class<?> type) {
    var array = Array.newInstance(type, 1);
    return Array.get(array, 0);
  }

  private E[] values;
  private int size;

  @SuppressWarnings("unchecked")
  public ValueList(Class<? extends E> valueClass) {
    if (!valueClass.isValue()) {
      throw new IllegalArgumentException("must be a value class");
    }
    if (NULL_RESTRICTED_ARRAY_AVAILABLE) {
      var defaultValue = DEFAULT_VALUE.get(valueClass);
      this.values = (E[]) ValueClass.newNullRestrictedAtomicArray(valueClass, 0, defaultValue);
      return;
    }
    this.values = (E[]) Array.newInstance(valueClass, 0);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  @SuppressWarnings("unchecked")
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
    var defaultValue = DEFAULT_VALUE.get(valueClass);
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
