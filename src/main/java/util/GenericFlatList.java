package util;

import java.io.IOException;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.value.ValueClass;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.LooselyConsistentValue;

public final class GenericFlatList<E> extends AbstractList<E> {
  record ValueClassInfo(boolean isAtomic, Object defaultValue) {}
  private static final boolean VALUE_CLASS_AVAILABLE;
  private static final ClassValue<ValueClassInfo> INFO_VALUE;
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

    ClassValue<ValueClassInfo> infoValue;
    try {
      infoValue = new ClassValue<>() {
        private static final Unsafe UNSAFE = Unsafe.getUnsafe();  // check that Unsafe is visible

        @Override
        protected ValueClassInfo computeValue(Class<?> type) {
          Object defaultValue;
          try {
            defaultValue = UNSAFE.allocateInstance(type);
          } catch (InstantiationException e) {
            throw new AssertionError(e);
          }
          var isAtomic = !isLooselyConsistentValue(type);
          return new ValueClassInfo(isAtomic, defaultValue);
        }
      };
    } catch (IllegalAccessError _) {
      infoValue = null;
      System.err.println("WARNING: default value is not available !");
    }
    INFO_VALUE = infoValue;
  }

  private static final String LOOSELY_CONSISTENT_VALUE_DESCRIPTOR =
      "Ljdk/internal/vm/annotation/LooselyConsistentValue;";
  static {
    assert LOOSELY_CONSISTENT_VALUE_DESCRIPTOR.equals(LooselyConsistentValue.class.descriptorString());
  }

  private static boolean isLooselyConsistentValue(Class<?> elementType) {
    var classFileName = elementType.getName().replace('.', '/') + ".class";
    ClassModel classModel;
    try (var classStream = elementType.getClassLoader().getResourceAsStream(classFileName)) {
      if (classStream == null) {
        throw new IllegalStateException("Could not find class file: " + classFileName);
      }
      classModel = ClassFile.of().parse(classStream.readAllBytes());
    } catch (IOException e) {
      throw new IllegalStateException("Error reading class file for " + elementType.getName(), e);
    }

    var visibleAnnotations = classModel.findAttribute(Attributes.runtimeVisibleAnnotations());
    if (visibleAnnotations.isPresent()) {
      var annotations = visibleAnnotations.orElseThrow();
      return annotations.annotations().stream()
          .anyMatch(
              annotation ->
                  LOOSELY_CONSISTENT_VALUE_DESCRIPTOR.equals(annotation.className().stringValue()));
    }
    return false;
  }

  private E[] values;
  private int size;

  @SuppressWarnings("unchecked")
  public GenericFlatList(Class<? extends E> elementType, int capacity) {
    if (VALUE_CLASS_AVAILABLE) {
      if (INFO_VALUE != null) {
        var classInfo = INFO_VALUE.get(elementType);
        values = (E[]) (classInfo.isAtomic ?
            ValueClass.newNullRestrictedAtomicArray(elementType, capacity, classInfo.defaultValue) :
            ValueClass.newNullRestrictedNonAtomicArray(elementType, capacity, classInfo.defaultValue));
        assert ValueClass.isFlatArray(values);
        return;
      }
      values = (E[]) ValueClass.newNullableAtomicArray(elementType, capacity);
      assert ValueClass.isFlatArray(values);
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
      if (INFO_VALUE != null) {
        var componentType = values.getClass().getComponentType();
        var defaultValue = INFO_VALUE.get(componentType).defaultValue;
        var newArray = (E[]) (ValueClass.isAtomicArray(values) ?
            ValueClass.newNullRestrictedAtomicArray(componentType, newCapacity, defaultValue) :
            ValueClass.newNullRestrictedNonAtomicArray(componentType, newCapacity, defaultValue));
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

  public boolean isFlat() {
    if (VALUE_CLASS_AVAILABLE) {
      return ValueClass.isFlatArray(values);
    }
    return false;
  }
}
