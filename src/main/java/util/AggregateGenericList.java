package util;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class AggregateGenericList<E> extends AbstractList<E> {
  private final int size;
  private final IntFunction<? extends E> mapper;

  public AggregateGenericList(int size, IntFunction<? extends E> mapper) {
    this.size = size;
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
