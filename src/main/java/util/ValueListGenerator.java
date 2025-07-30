package util;

import jdk.internal.misc.Unsafe;
import jdk.internal.value.ValueClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;

import java.lang.classfile.*;
import java.lang.constant.*;

import static java.lang.classfile.ClassFile.ACC_FINAL;
import static java.lang.classfile.ClassFile.ACC_IDENTITY;
import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.ACC_STRICT;
import static java.lang.classfile.ClassFile.JAVA_25_VERSION;
import static java.lang.constant.ConstantDescs.CD_Class;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.invoke.MethodHandles.Lookup.ClassOption.NESTMATE;
import static java.lang.invoke.MethodHandles.Lookup.ClassOption.STRONG;

final class ValueListGenerator {
  /*
  record Point(int x, int y) {}

  public final class ValueListImpl implements ValueListOld<Point> {
    private Point[] array;
    private int size;

    private static Point[] newArray(int capacity) {
      return new Point[capacity];
    }

    public ValueListImpl(int capacity) {
      this.array = newArray(capacity);
    }

    public ValueListImpl() {
      this(16);
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public Point get(int index) {
      Objects.checkIndex(index, size);
      return array[index];
    }

    @Override
    public Point set(int index, Point element) {
      Objects.checkIndex(index, size);
      var old = array[index];
      array[index] = element;
      return old;
    }

    private void resize() {
      var newArray = newArray(array.length << 1);
      System.arraycopy(array, 0, newArray, 0, array.length);
      array = newArray;
    }

    @Override
    public boolean add(Point point) {
      if (size == array.length) {
        resize();
      }
      array[size++] = point;
      return true;
    }
  }*/

  private static final ClassDesc CD_VALUE_LIST = ClassDesc.of(ValueList.class.getName());

  public static byte[] generateValueListImpl(Class<?> lookupClass, Class<?> elementType) {
    var elementDesc = ClassDesc.of(elementType.getName());
    var thisClass = ClassDesc.of(lookupClass.getPackageName(), "ValueListImpl");
    return ClassFile.of()
        .build(thisClass,
        classBuilder -> {
          // Class declaration: public final class ValueListImpl implements ValueList<ElementType>
          classBuilder
              .withVersion(JAVA_25_VERSION, 0)
              .withFlags(ACC_PUBLIC | ACC_FINAL | ACC_IDENTITY)
              .withSuperclass(CD_Object)
              .withInterfaceSymbols(CD_VALUE_LIST);

          // Fields
          generateFields(classBuilder, elementDesc);

          // Methods
          generateConstructors(classBuilder, thisClass, elementDesc);
          generateSizeMethod(classBuilder, thisClass);
          generateGetMethod(classBuilder, thisClass, elementDesc);
          generateSetMethod(classBuilder, thisClass, elementDesc);
          generateResizeMethod(classBuilder, thisClass, elementDesc);
          generateAddMethod(classBuilder, thisClass, elementDesc);

          generateEqualsMethod(classBuilder);
          generateHashCodeMethod(classBuilder);
          generateToStringMethod(classBuilder);
        }
    );
  }

  private static void generateFields(ClassBuilder classBuilder, ClassDesc elementDesc) {
    // Field: private Point[] array
    classBuilder.withField("array", elementDesc.arrayType(),
        fieldBuilder -> fieldBuilder.withFlags(ACC_PRIVATE | ACC_STRICT));

    // Field: private int size
    classBuilder.withField("size", CD_int,
        fieldBuilder -> fieldBuilder.withFlags(ACC_PRIVATE | ACC_STRICT));
  }

  private static void generateConstructors(ClassBuilder classBuilder, ClassDesc thisClass, ClassDesc elementDesc) {
    // Constructor: public ValueListImpl(int capacity)
    classBuilder.withMethod("<init>",
        MethodTypeDesc.of(CD_void, CD_int),
        ACC_PUBLIC,
        methodBuilder -> methodBuilder.withCode(codeBuilder -> {
          var elementArrayDesc = elementDesc.arrayType();
          codeBuilder
              .aload(0)  // load this
              .ldc(elementDesc)  // load elementType
              .iload(1)  // load capacity
              .invokestatic(CD_VALUE_LIST, "newArray",
                  MethodTypeDesc.of(CD_Object.arrayType(), CD_Class, CD_int), true)
              .checkcast(elementArrayDesc)
              .putfield(thisClass, "array", elementArrayDesc)
              .aload(0)  // load this
              .iconst_0()     // load 0
              .putfield(thisClass, "size", CD_int)
              .aload(0)  // load this
              .invokespecial(CD_Object, "<init>", MethodTypeDesc.of(CD_void))  // super()
              .return_();
        }));

    // Constructor: public ValueListImpl()
    classBuilder.withMethod("<init>",
        MethodTypeDesc.of(CD_void),
        ACC_PUBLIC,
        methodBuilder -> methodBuilder.withCode(codeBuilder -> {
          codeBuilder
              .aload(0)  // load this
              .bipush(16)  // load default capacity
              .invokespecial(thisClass, "<init>",
                  MethodTypeDesc.of(CD_void, CD_int))
              .return_();
        }));
  }

  private static void generateSizeMethod(ClassBuilder classBuilder, ClassDesc thisClass) {
    // Method: public int size()
    classBuilder.withMethod("size",
        MethodTypeDesc.of(CD_int),
        ACC_PUBLIC,
        methodBuilder -> methodBuilder.withCode(codeBuilder -> {
          codeBuilder
              .aload(0)  // load this
              .getfield(thisClass, "size", CD_int)
              .ireturn();
        }));
  }

  private static void generateGetMethod(ClassBuilder classBuilder, ClassDesc thisClass, ClassDesc elementDesc) {
    // Method: public Object get(int index)
    classBuilder.withMethod("get",
        MethodTypeDesc.of(CD_Object, CD_int),
        ACC_PUBLIC,
        methodBuilder -> methodBuilder.withCode(codeBuilder -> {
          codeBuilder
              .iload(1)  // load index
              .aload(0)  // load this
              .getfield(thisClass, "size", CD_int)
              .invokestatic(ClassDesc.of("java.util.Objects"), "checkIndex",
                  MethodTypeDesc.of(CD_int, CD_int, CD_int))
              .pop()  // discard return value
              .aload(0)  // load this
              .getfield(thisClass, "array", elementDesc.arrayType())
              .iload(1)  // load index
              .aaload()  // get array element
              .areturn();
        }));
  }

  private static void generateSetMethod(ClassBuilder classBuilder, ClassDesc thisClass, ClassDesc elementDesc) {
    // Method: public Object set(int index, Object element)
    classBuilder.withMethod("set",
        MethodTypeDesc.of(CD_Object, CD_int, CD_Object),
        ACC_PUBLIC,
        methodBuilder -> methodBuilder.withCode(codeBuilder -> {
          var elementArrayDesc = elementDesc.arrayType();
          codeBuilder
              .iload(1)  // load index
              .aload(0)  // load this
              .getfield(thisClass, "size", CD_int)
              .invokestatic(ClassDesc.of("java.util.Objects"), "checkIndex",
                  MethodTypeDesc.of(CD_int, CD_int, CD_int))
              .pop()  // discard return value
              .aload(0)  // load this
              .getfield(thisClass, "array", elementArrayDesc)
              .iload(1)  // load index
              .aaload()  // get old value
              .astore(3)  // store old value in a local variable
              .aload(0)  // load this
              .getfield(thisClass, "array", elementArrayDesc)
              .iload(1)  // load index
              .aload(2)  // load new element
              .checkcast(elementDesc)
              .aastore()  // set array element
              .aload(3)  // load old value
              .areturn();
        }));
  }

  private static void generateResizeMethod(ClassBuilder classBuilder, ClassDesc thisClass, ClassDesc elementDesc) {
    // Method: private void resize()
    classBuilder.withMethod("resize",
        MethodTypeDesc.of(CD_void),
        ACC_PRIVATE,
        methodBuilder -> methodBuilder.withCode(codeBuilder -> {
          var elementArrayDesc = elementDesc.arrayType();
          codeBuilder
              .aload(0)  // load this
              .getfield(thisClass, "array", elementArrayDesc)
              .aload(0)  // load this
              .getfield(thisClass, "array", elementArrayDesc)
              .arraylength()
              .iconst_1()
              .ishl()  // << 1 (double the size)
              .invokestatic(CD_VALUE_LIST, "arrayCopy",
                  MethodTypeDesc.of(CD_Object.arrayType(), CD_Object.arrayType(), CD_int), true)
              .checkcast(elementArrayDesc)
              .astore(1)  // store the new array in a local variable
              .aload(0)  // load this
              .aload(1)  // load newArray
              .putfield(thisClass, "array", elementArrayDesc)
              .return_();
        }));
  }

  private static void generateAddMethod(ClassBuilder classBuilder, ClassDesc thisClass, ClassDesc elementDesc) {
    // Method: public boolean add(Object point)
    classBuilder.withMethod("add",
        MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_Object),
        ACC_PUBLIC,
        methodBuilder -> methodBuilder.withCode(codeBuilder -> {
          var elementArrayDesc = elementDesc.arrayType();
          var skipResizeLabel = codeBuilder.newLabel();
          codeBuilder
              .aload(0)  // load this
              .getfield(thisClass, "size", CD_int)
              .aload(0)  // load this
              .getfield(thisClass, "array", elementArrayDesc)
              .arraylength()
              .if_icmpne(skipResizeLabel)  // if size != array.length, jump to addElement
              .aload(0)  // load this
              .invokespecial(thisClass, "resize", MethodTypeDesc.of(CD_void))
              .labelBinding(skipResizeLabel)
              .aload(0)  // load this
              .getfield(thisClass, "array", elementArrayDesc)
              .aload(0)  // load this
              .dup()  // duplicate this for later use
              .getfield(thisClass, "size", CD_int)
              .dup_x1()  // duplicate size and put it under this  FIXME ?
              .iconst_1()
              .iadd()  // size + 1
              .putfield(thisClass, "size", CD_int)  // this.size = size + 1
              .aload(1)  // load point parameter
              .checkcast(elementDesc)
              .aastore()  // array[size] = point
              .iconst_1()  // return true
              .ireturn();
        }));
  }

  private static void generateEqualsMethod(ClassBuilder classBuilder) {
    classBuilder.withMethod("equals", MethodTypeDesc.of(CD_boolean, CD_Object), ACC_PUBLIC, mb -> {
      mb.withCode(codeBuilder -> {
        codeBuilder
            .aload(0)  // this
            .aload(1)  // obj parameter
            .invokestatic(CD_VALUE_LIST, "defaultEquals",
                MethodTypeDesc.of(CD_boolean, CD_VALUE_LIST, CD_Object), true)
            .ireturn();
      });
    });
  }

  private static void generateHashCodeMethod(ClassBuilder classBuilder) {
    classBuilder.withMethod("hashCode", MethodTypeDesc.of(CD_int), ACC_PUBLIC, mb -> {
      mb.withCode(codeBuilder -> {
        codeBuilder
            .aload(0)  // this
            .invokestatic(CD_VALUE_LIST, "defaultHashCode",
                MethodTypeDesc.of(CD_int, CD_VALUE_LIST), true)
            .ireturn();
      });
    });
  }

  private static void generateToStringMethod(ClassBuilder classBuilder) {
    classBuilder.withMethod("toString", MethodTypeDesc.of(CD_String), ACC_PUBLIC, mb -> {
      mb.withCode(codeBuilder -> {
        codeBuilder
            .aload(0)  // this
            .invokestatic(CD_VALUE_LIST, "defaultToString",
                MethodTypeDesc.of(CD_String, CD_VALUE_LIST), true)
            .areturn();
      });
    });
  }

  private static final boolean VALUE_CLASS_AVAILABLE;
  private static final ClassValue<Object> DEFAULT_VALUE;
  static {
    boolean valueClassAvailable;
    try {
      var _ = ValueClass.class;  // check that ValueClass is visible
      valueClassAvailable = true;
    } catch (IllegalAccessError _) {
      valueClassAvailable = false;
      System.err.println("WARNING: ValueClass class is not available !");
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

  @SuppressWarnings("unchecked")
  static <E> E[] newArray(Class<E> elementType, int capacity) {
    if (VALUE_CLASS_AVAILABLE) {
      if (DEFAULT_VALUE != null) {
        return (E[]) ValueClass.newNullRestrictedAtomicArray(elementType, capacity, DEFAULT_VALUE.get(elementType));
      }
      return (E[]) ValueClass.newNullableAtomicArray(elementType, capacity);
    }
    return (E[]) Array.newInstance(elementType, capacity);
  }

  @SuppressWarnings("unchecked")
  static <E> E[] arrayCopy(E[] array, int newCapacity) {
    if (VALUE_CLASS_AVAILABLE) {
      if (DEFAULT_VALUE != null) {
        var componentType = array.getClass().getComponentType();
        var newArray = (E[]) ValueClass.newNullRestrictedAtomicArray(componentType, newCapacity, DEFAULT_VALUE.get(componentType));
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
      }
      return (E[]) ValueClass.copyOfSpecialArray(array, 0, newCapacity);
    }
    return Arrays.copyOf(array, newCapacity);
  }

  private static final class Cache {
    private final MethodHandles.Lookup hiddenLookup;
    private MethodHandle defaultConstructor;
    private MethodHandle primaryConstructor;

    private Cache(MethodHandles.Lookup hiddenLookup) {
      this.hiddenLookup = hiddenLookup;
    }

    MethodHandle defaultConstructor() {
      if (defaultConstructor != null) {
        return defaultConstructor;
      }
      return defaultConstructor = constructor(MethodType.methodType(void.class));
    }

    MethodHandle primaryConstructor() {
      if (primaryConstructor != null) {
        return primaryConstructor;
      }
      return primaryConstructor = constructor(MethodType.methodType(void.class, int.class));
    }

    private MethodHandle constructor(MethodType methodType) {
      MethodHandle constructor;
      try {
        constructor = hiddenLookup.findConstructor(hiddenLookup.lookupClass(), methodType);
      } catch (NoSuchMethodException e) {
        throw (NoSuchMethodError) new NoSuchMethodError().initCause(e);
      } catch (IllegalAccessException e) {
        throw (IllegalAccessError) new IllegalAccessError().initCause(e);
      }
      return constructor.asType(constructor.type().changeReturnType(ValueList.class));
    }
  }

  private static Cache createCache(MethodHandles.Lookup lookup, Class<?> elementType) {
    var classBytes = generateValueListImpl(lookup.lookupClass(), elementType);
    MethodHandles.Lookup hiddenLookup;
    try {
      hiddenLookup = lookup.defineHiddenClass(classBytes, true, NESTMATE, STRONG);
    } catch (IllegalAccessException e) {
      throw (IllegalAccessError) new IllegalAccessError().initCause(e);
    }
    return new Cache(hiddenLookup);
  }

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final ClassValue<Cache> VALUE_LIST_CACHE = new ClassValue<>() {
    @Override
    protected Cache computeValue(Class<?> type) {
      MethodHandles.Lookup lookup;
      try {
        LOOKUP.accessClass(type);
        lookup = LOOKUP;
      } catch (IllegalAccessException e) {
        try {
          lookup = MethodHandles.privateLookupIn(type, LOOKUP);
        } catch (IllegalAccessException e2) {
          throw (IllegalAccessError) new IllegalAccessError().initCause(e2);
        }
      }
      return createCache(lookup, type);
    }
  };

  @SuppressWarnings("unchecked")
  static <E> ValueList<E> createValueList(Class<? extends E> elementType) {
    var cache = VALUE_LIST_CACHE.get(elementType);
    var constructor = cache.defaultConstructor();
    try {
      return (ValueList<E>) constructor.invokeExact();
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  @SuppressWarnings("unchecked")
  static <E> ValueList<E> createValueList(Class<? extends E> elementType, int capacity) {
    var cache = VALUE_LIST_CACHE.get(elementType);
    var constructor = cache.primaryConstructor();
    try {
      return (ValueList<E>) constructor.invokeExact(capacity);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new UndeclaredThrowableException(e);
    }
  }



  static <E> ValueList.Factory<E> factory(MethodHandles.Lookup lookup, Class<E> elementType) {
    record FactoryImpl<E>(MethodHandle defaultConstructor, MethodHandle primaryConstructor) implements ValueList.Factory<E> {
      @Override
      @SuppressWarnings("unchecked")
      public ValueList<E> create() {
        try {
          return (ValueList<E>) defaultConstructor.invokeExact();
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public ValueList<E> create(int capacity) {
        try {
          return (ValueList<E>) primaryConstructor.invokeExact(capacity);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }
    }

    var cache = createCache(lookup, elementType);
    return new FactoryImpl<>(cache.defaultConstructor(), cache.primaryConstructor());
  }

  public static void main(String[] args) throws Throwable {
    // Generate class for String type
    var elementType = String.class;
    var lookup = MethodHandles.lookup();
    var classBytes = generateValueListImpl(lookup.lookupClass(), elementType);

    var hiddenLookup = lookup.defineHiddenClass(classBytes, true, NESTMATE, STRONG);
    var constructor = hiddenLookup.findConstructor(hiddenLookup.lookupClass(), MethodType.methodType(void.class));
    var mh = constructor.asType(constructor.type().changeReturnType(ValueList.class));

    @SuppressWarnings("unchecked")
    List<String> list = (ValueList<String>) mh.invokeExact();

    list.add("Hello");
    list.add("World");

    System.out.println(list);
  }
}
