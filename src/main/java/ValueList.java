import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;

final class ValueList<E> extends AbstractList<E> {
  private E[] values;
  private int size;

  @SuppressWarnings("unchecked")
  public ValueList(Class<? extends E> valueClass) {
    if (!valueClass.isValue()) {
      throw new IllegalArgumentException("must be a value class");
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
    return (E) Array.get(values, index);
  }

  @Override
  public boolean add(E element) {
    if (size == values.length) {
      values = Arrays.copyOf(values, Math.max(16, values.length << 1));
    }
    Array.set(values, size++, element);
    return true;
  }
}
