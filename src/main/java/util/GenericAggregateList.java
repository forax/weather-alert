package util;

import java.util.AbstractList;
import java.util.Objects;
import java.util.function.IntFunction;

public final class GenericAggregateList<E> extends AbstractList<E> {
  private final int size;
  private final IntFunction<? extends E> mapper;

  public GenericAggregateList(int size, IntFunction<? extends E> mapper) {
    this.size = size;
    if (size < 0) {
      throw new IllegalArgumentException("Size cannot be negative");
    }
    this.mapper = Objects.requireNonNull(mapper);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public E get(int index) {
    Objects.checkIndex(index, size);
    return mapper.apply(index);
  }
}
