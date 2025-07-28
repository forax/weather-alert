package util;

import java.lang.invoke.MethodHandles;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public interface AggregateList<E> extends List<E> {
  E get(int index);

  E set(int index, E element);

  int size();

  @Override
  default int indexOf(Object o) {
    if (o == null) {
      return indexOfNull();
    }
    for (var i = 0; i < size(); i++) {
      if (o.equals(get(i))) {
        return i;
      }
    }
    return -1;
  }

  private int indexOfNull() {
    for (var i = 0; i < size(); i++) {
      if (get(i) == null) {
        return i;
      }
    }
    return -1;
  }

  @Override
  default int lastIndexOf(Object o) {
    if (o == null) {
      return lastIndexOfNull();
    }
    for (var i = size(); --i >= 0; ) {
      if (o.equals(get(i))) {
        return i;
      }
    }
    return -1;
  }

  private int lastIndexOfNull() {
    for (var i = size(); --i >= 0; ) {
      if (get(i) == null) {
        return i;
      }
    }
    return -1;
  }

  private AbstractList<E> asList() { // Delegate to AbstractList
    return new AbstractList<>() {
      @Override
      public int size() {
        return util.AggregateList.this.size();
      }

      @Override
      public E get(int index) {
        return util.AggregateList.this.get(index);
      }
    };
  }

  @Override
  default ListIterator<E> listIterator() {
    return listIterator(0);
  }

  @Override
  default ListIterator<E> listIterator(int index) {
    return asList().listIterator(index);
  }

  @Override
  default List<E> subList(int fromIndex, int toIndex) {
    Objects.checkFromToIndex(fromIndex, toIndex, size());
    return asList().subList(fromIndex, toIndex);
  }

  @Override
  default boolean isEmpty() {
    return size() == 0;
  }

  @Override
  default boolean contains(Object o) {
    return indexOf(o) != -1;
  }

  @Override
  default Iterator<E> iterator() {
    var size = size();
    return new Iterator<E>() {
      private int index;

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return get(index++);
      }
    };
  }

  @Override
  default Object[] toArray() {
    var size = size();
    var array = new Object[size];
    for (var i = 0; i < size; i++) {
      array[i] = get(i);
    }
    return array;
  }

  @Override
  default <T> T[] toArray(T[] a) {
    return asList().toArray(a);
  }

  @Override
  default boolean containsAll(Collection<?> c) {
    return new HashSet<>(this).containsAll(c);
  }

  @Override
  default void add(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  default E remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean add(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean addAll(Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean addAll(int index, Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  default void clear() {
    throw new UnsupportedOperationException();
  }

  static boolean defaultEquals(util.AggregateList<?> list, Object o) { // called by generated code
    Objects.requireNonNull(list);
    if (!(o instanceof List<?> list2) || list.size() != list2.size()) {
      return false;
    }
    var it1 = list.iterator();
    var it2 = list2.iterator();
    while (it1.hasNext() && it2.hasNext()) {
      var e1 = it1.next();
      var e2 = it2.next();
      if (!Objects.equals(e1, e2)) {
        return false;
      }
    }
    return !(it1.hasNext() || it2.hasNext());
  }

  static int defaultHashCode(util.AggregateList<?> list) { // called by generated code
    Objects.requireNonNull(list);
    var hashCode = 1;
    for (var i = 0; i < list.size(); i++) {
      var e = list.get(i);
      hashCode = 31 * hashCode + Objects.hashCode(e);
    }
    return hashCode;
  }

  static String defaultToString(util.AggregateList<?> list) { // called by generated code
    Objects.requireNonNull(list);
    return list.asList().toString();
  }

  interface Factory<E> {
    AggregateList<E> create(List<?> list0);
    AggregateList<E> create(List<?> list0, List<?> list1);
    AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2);
    AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3);
    AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3, List<?> list4);
    AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3, List<?> list4, List<?> list5);
    AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3, List<?> list4, List<?> list5, List<?> list6);
    AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3, List<?> list4, List<?> list5, List<?> list6, List<?> list7);
  }

  static <E> AggregateList.Factory<E> factory(MethodHandles.Lookup lookup, Class<E> recordType) {
    Objects.requireNonNull(lookup);
    Objects.requireNonNull(recordType);
    if (!recordType.isRecord()) {
      throw new IllegalArgumentException("must be a record");
    }
    return AggregateListGenerator.factory(lookup, recordType);
  }
}
