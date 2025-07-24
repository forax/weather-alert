package weather;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AggregateList<E extends Record> extends AbstractList<E> {
  private final List<? extends List<?>> list;
  private final Constructor<E> constructor;

  private static <E extends Record> Constructor<E> findConstructor(Class<E> elementType) {
    var types = Arrays.stream(elementType.getRecordComponents())
        .map(RecordComponent::getType)
        .toArray(Class<?>[]::new);
    Constructor<E> constructor;
    try {
      constructor = elementType.getDeclaredConstructor(types);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    constructor.setAccessible(true);
    return constructor;
  }

  public AggregateList(List<? extends List<?>> list,  Class<E> elementType) {
    Objects.requireNonNull(list, "list must not be null");
    Objects.requireNonNull(elementType, "elementType must not be null");
    if (!elementType.isRecord()) {
      throw new IllegalArgumentException(elementType.getName() + " is not a record");
    }
    if (elementType.getRecordComponents().length != list.size()) {
      throw new IllegalArgumentException("List size and record component size mismatch");
    }
    for(var i = 1; i < list.size(); i++) {
      if (list.getFirst().size() != list.get(i).size()) {
        throw new IllegalArgumentException("different Lists size mismatch");
      }
    }
    this.list = list;
    this.constructor = findConstructor(elementType);
  }

  @Override
  public int size() {
    return list.getFirst().size();
  }

  @Override
  public E get(int index) {
    var values = new Object[list.size()];
    for (var i = 0; i < values.length; i++) {
      values[i] = list.get(i).get(index);
    }
    try {
      return constructor.newInstance(values);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
