import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.value.ValueClass;

final class ValueList<E> extends AbstractList<E> {
  private static Object defaultIdentityValue(Class<?> type) {
    var array = Array.newInstance(type, 1);
    return Array.get(array, 0);
  }

  private static final ClassValue<Object> DEFAULT_VALUE = new ClassValue<>() {
    @Override
    protected Object computeValue(Class<?> type) {
      if (!type.isValue()) {
        return defaultIdentityValue(type);
      }
      Class<?>[] types;
      Constructor<?> constructor;
      if (type.isRecord()) {
        types = Arrays.stream(type.getRecordComponents())
            .map(RecordComponent::getType)
            .toArray(Class<?>[]::new);
        try {
          constructor = type.getDeclaredConstructor(types);
        } catch (NoSuchMethodException e) {
          throw new AssertionError(e);
        }
      } else {
        var constructors = type.getDeclaredConstructors();
        if (constructors.length != 1) {
          return defaultIdentityValue(type);
        }
        constructor = constructors[0];
        types = constructor.getParameterTypes();
      }
      var defaultValues = Arrays.stream(types)
          .map(DEFAULT_VALUE::get)
          .toArray();
      try {
        constructor.setAccessible(true);
        return constructor.newInstance(defaultValues);
      } catch (InvocationTargetException | InaccessibleObjectException _) {
        return defaultIdentityValue(type);
      } catch(IllegalAccessException | InstantiationException e) {
        throw new AssertionError(e);
      }
    }
  };

  private E[] values;
  private int size;

  @SuppressWarnings("unchecked")
  public ValueList(Class<? extends E> valueClass) {
    if (!valueClass.isValue()) {
      throw new IllegalArgumentException("must be a value class");
    }
    var defaultValue = DEFAULT_VALUE.get(valueClass);
    this.values = (E[]) (defaultValue == null ?
        Array.newInstance(valueClass, 0) :
        ValueClass.newNullRestrictedAtomicArray(valueClass, 0, defaultValue));
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E get(int index) {
    Objects.checkIndex(index, size);
    return (E) Array.get(values, index);
  }

  private void resize() {
    var newSize = Math.max(16, values.length << 1);
    if (ValueClass.isNullRestrictedArray(values)) {
      var valueClass = values.getClass().getComponentType();
      var defaultValue = DEFAULT_VALUE.get(valueClass);
      @SuppressWarnings("unchecked")
      var newArray = (E[]) ValueClass.newNullRestrictedAtomicArray(valueClass, newSize, defaultValue);
      System.arraycopy(values, 0, newArray, 0, size);
      values = newArray;
    } else {
      values = Arrays.copyOf(values, newSize);
    }
  }

  private void expandToNullableArray() {
    var valueClass = values.getClass().getComponentType();
    @SuppressWarnings("unchecked")
    var newArray = (E[]) Array.newInstance(valueClass, values.length);
    System.arraycopy(values, 0, newArray, 0, size);
    values = newArray;
  }

  @Override
  public boolean add(E element) {
    if (size == values.length) {
      resize();
    }
    if (element == null && ValueClass.isNullRestrictedArray(values)) {
      expandToNullableArray();
    }
    Array.set(values, size++, element);
    return true;
  }
}
