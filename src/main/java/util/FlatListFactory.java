package util;

import jdk.internal.misc.Unsafe;
import jdk.internal.value.ValueClass;

import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.JAVA_26_VERSION;
import static java.lang.classfile.ClassFile.PREVIEW_MINOR_VERSION;
import static java.lang.constant.ConstantDescs.*;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.invoke.MethodHandles.Lookup.ClassOption.NESTMATE;
import static java.lang.invoke.MethodHandles.Lookup.ClassOption.STRONG;

/**
 * A factory that creates specialized List implementations for different types using a single
 * bytecode template with ClassData specialization via hidden classes.
 */
public final class FlatListFactory {
  private FlatListFactory() {
    throw new AssertionError();
  }


  public static final int NON_FLAT = 1;
  public static final int FLAT = 2;
  public static final int NON_NULL_FLAT = 3;
  public static final int NON_ATOMIC_FLAT = 4;

  public static <T> List<T> create(Class<T> elementType) {
    Objects.requireNonNull(elementType);
    return create(elementType, FLAT);
  }

  /** Creates a new specialized List for the given element type */
  @SuppressWarnings("unchecked")
  public static <T> List<T> create(Class<? extends T> elementType, int properties, int initialCapacity) {
    Objects.requireNonNull(elementType);
    if (properties < NON_FLAT || properties > NON_ATOMIC_FLAT)  {
      throw new IllegalArgumentException("Invalid properties: " + properties);
    }
    if (initialCapacity < 1) {
     throw new IllegalArgumentException("Invalid initialCapacity: " + initialCapacity);
    }
    var erasedType = elementType.isValue() ? elementType : Object.class;
    var cache = SPECIALIZED_CONSTRUCTORS.get(erasedType);
    var constructor = cache.constructor(erasedType, properties);
    try {
      return (List<T>) constructor.invokeExact(initialCapacity);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public static <T> List<T> create(Class<? extends T> elementType, int properties) {
    return create(elementType, properties, 16);
  }

  public static boolean isFlat(List<?> list) {
    Objects.requireNonNull(list);
    if (!(list instanceof AbstractFlatList<?> flatList)) {
      return false;
    }
    return flatList.isFlat();
  }

  private record Specialization(Class<?> elementType, int properties, Object defaultValue) {}

  private static final ClassDesc CD_ABSTRACT_FLAT_LIST = ClassDesc.of(AbstractFlatList.class.getName());
  private static final ClassDesc CD_VALUE_CLASS = ClassDesc.of(ValueClass.class.getName());
  private static final ClassDesc CD_SPECIALIZATION = ClassDesc.of(Specialization.class.getName());
  private static final ClassDesc CD_FACTORY = ClassDesc.of(FlatListFactory.class.getName());
  private static final DynamicConstantDesc<Object> SPECIALIZATION =
      DynamicConstantDesc.ofNamed(ConstantDescs.BSM_CLASS_DATA, "_", CD_SPECIALIZATION);
  private static final DynamicConstantDesc<Object> ELEMENT_ARRAY_CLASS =
      DynamicConstantDesc.ofNamed(
          ofConstantBootstrap(CD_FACTORY, "arrayType", CD_Class, CD_SPECIALIZATION),
          "_", ConstantDescs.CD_Class, SPECIALIZATION);

  private static final ClassDesc TEMPLATE_CLASS = ClassDesc.of(FlatListFactory.class.getPackageName(), "TemplateList");
  private static final byte[] TEMPLATE_BYTECODE = generateTemplateBytecode();

  // FIXME use either @Stable or stable value
  private static final class Cache {
    private /*@Stable*/ MethodHandle flat, nonFlat, nonNull,nonAtomicNonNull;

    private Cache() {}

    public MethodHandle constructor(Class<?> elementType, int properties) {
      return switch (properties) {
        case NON_FLAT -> {
          if (nonFlat != null) {
            yield nonFlat;
          }
          yield nonFlat = createConstructor(elementType, NON_FLAT);
        }
        case FLAT -> {
          if (flat != null) {
            yield flat;
          }
          yield flat = createConstructor(elementType, FLAT);
        }
        case NON_NULL_FLAT -> {
          if (nonNull != null) {
            yield nonNull;
          }
          yield nonNull = createConstructor(elementType, NON_NULL_FLAT);
        }
        case NON_ATOMIC_FLAT -> {
          if (nonAtomicNonNull != null) {
            yield nonAtomicNonNull;
          }
          yield nonAtomicNonNull = createConstructor(elementType, NON_ATOMIC_FLAT);
        }
        default -> throw new IllegalArgumentException("Unknown properties: " + properties);
      };
    }
  }

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  private static MethodHandle createConstructor(Class<?> elementType, int properties) {
    var specialization = new Specialization(elementType, properties, defaultValue(elementType, properties));
    try {
      // Define hidden class with specialization as ClassData
      var hiddenLookup =
          LOOKUP.defineHiddenClassWithClassData(TEMPLATE_BYTECODE, specialization, true, NESTMATE, STRONG);

      // Get constructor
      return hiddenLookup
          .findConstructor(hiddenLookup.lookupClass(), MethodType.methodType(void.class, int.class))
          .asType(MethodType.methodType(List.class, int.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  private static Object defaultValue(Class<?> elementType, int properties) {
    if (properties == NON_NULL_FLAT || properties == NON_ATOMIC_FLAT) {
      return DefaultValue.defaultValue(elementType);
    }
    return null;
  }

  private static final class DefaultValue {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    private static Object defaultValue(Class<?> type) {
      try {
        return UNSAFE.allocateInstance(type);
      } catch (InstantiationException e) {
        throw new AssertionError(e);
      }
    }
  }

  /** ClassValue to cache generated constructor MethodHandles per element type */
  private static final ClassValue<Cache> SPECIALIZED_CONSTRUCTORS = new ClassValue<>() {
        @Override
        protected Cache computeValue(Class<?> elementType) {
          return new Cache();
        }
      };

  private static abstract class AbstractFlatList<E> extends AbstractList<E> {
    private AbstractFlatList() {}

    abstract boolean isFlat();
  }

  /** Generates the template bytecode that will be specialized with ClassData */
  private static byte[] generateTemplateBytecode() {
    return ClassFile.of()
        .build(
            TEMPLATE_CLASS,
            classBuilder -> {
              classBuilder
                  .withVersion(JAVA_26_VERSION, PREVIEW_MINOR_VERSION)
                  .withFlags(ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_IDENTITY)
                  .withSuperclass(CD_ABSTRACT_FLAT_LIST)
                  .withInterfaceSymbols(ConstantDescs.CD_List);

              // Fields - use Object[] since we don't know the specific type at generation time
              classBuilder
                  .withField("array", CD_Object.arrayType(), ACC_PRIVATE)
                  .withField("size", CD_int, ACC_PRIVATE);

              // method void <init>()
              generateTemplateConstructor(classBuilder);

              // method boolean add(Object element))
              generateTemplateAddMethod(classBuilder);

              // method void resize()
              generateTemplateResizeMethod(classBuilder);

              // method Object get(int)
              generateTemplateGetMethod(classBuilder);

              // method int size()
              generateTemplateSizeMethod(classBuilder);

              // method boolean isFlat()
              generateTemplateIsFlatMethod(classBuilder);
            });
  }

  private static void generateTemplateConstructor(ClassBuilder classBuilder) {
    classBuilder.withMethod(
        INIT_NAME, MethodTypeDesc.of(CD_void, CD_int),
        ACC_PUBLIC,
        methodBuilder ->
            methodBuilder.withCode(codeBuilder -> {
                  // Call super()
                  codeBuilder
                      .aload(0)
                      .invokespecial(CD_ABSTRACT_FLAT_LIST, INIT_NAME, MethodTypeDesc.of(CD_void));

                  // Initialize array field with the initial capacity
                  codeBuilder
                      .aload(0)
                      .ldc(SPECIALIZATION)
                      .iload(1)  // initialCapacity
                      .invokestatic(
                          CD_FACTORY,
                          "newArray",
                          MethodTypeDesc.of(CD_Object.arrayType(), CD_SPECIALIZATION, CD_int))
                      .putfield(TEMPLATE_CLASS, "array", CD_Object.arrayType())
                      .return_();
                }));
  }

  private static void generateTemplateAddMethod(ClassBuilder classBuilder) {
    classBuilder.withMethod(
        "add", MethodTypeDesc.of(ConstantDescs.CD_boolean, CD_Object),
        ACC_PUBLIC,
        methodBuilder ->
            methodBuilder.withCode(
                codeBuilder -> {
                  // Check if resize is needed
                  var noResize = codeBuilder.newLabel();

                  codeBuilder
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "size", CD_int)
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "array", CD_Object.arrayType())
                      .arraylength()
                      .if_icmplt(noResize);

                  // Slow path, call resize
                  // Use Arrays.copyOf to create new array with double capacity
                  codeBuilder
                      .aload(0)
                      .invokevirtual(TEMPLATE_CLASS, "resize", MethodTypeDesc.of(CD_void));

                  codeBuilder.labelBinding(noResize);

                  // array[size] = element
                  codeBuilder
                      .loadConstant(ELEMENT_ARRAY_CLASS)  // Add a cast to the real array type to help the JIT
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "array", CD_Object.arrayType())
                      .invokevirtual(CD_Class, "cast", MethodTypeDesc.of(CD_Object, CD_Object))
                      .checkcast(CD_Object.arrayType())
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "size", CD_int)
                      .aload(1)
                      .aastore();

                  // size++
                  codeBuilder
                      .aload(0)
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "size", CD_int)
                      .iconst_1()
                      .iadd()
                      .putfield(TEMPLATE_CLASS, "size", CD_int);

                  // return true
                  codeBuilder.iconst_1().ireturn();
                }));
  }

  private static void generateTemplateResizeMethod(ClassBuilder classBuilder) {
    classBuilder.withMethod(
        "resize", MethodTypeDesc.of(ConstantDescs.CD_void),
        ACC_PRIVATE,
        methodBuilder ->
            methodBuilder.withCode(codeBuilder -> {
                  // Slow path. Use arrayResize to create new array with double capacity
                  codeBuilder
                      .aload(0)
                      .ldc(SPECIALIZATION)
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "array", CD_Object.arrayType())
                      .invokestatic(
                          CD_FACTORY,
                          "arrayResize",
                          MethodTypeDesc.of(CD_Object.arrayType(), CD_SPECIALIZATION, CD_Object.arrayType()))
                      .putfield(TEMPLATE_CLASS, "array", CD_Object.arrayType())
                      .return_();
                }));
  }

  private static void generateTemplateGetMethod(ClassBuilder classBuilder) {
    classBuilder.withMethod(
        "get", MethodTypeDesc.of(CD_Object, CD_int),
        ACC_PUBLIC,
        methodBuilder ->
            methodBuilder.withCode(codeBuilder -> {
                  // Use Objects.checkIndex for bounds checking
                  codeBuilder
                      .iload(1) // index
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "size", CD_int)  // size
                      .invokestatic(
                          ClassDesc.of("java.util.Objects"),
                          "checkIndex",
                          MethodTypeDesc.of(CD_int, CD_int, CD_int))
                      .pop();

                  // Return array[index]
                  codeBuilder
                      .loadConstant(ELEMENT_ARRAY_CLASS)  // Add a cast to the real array type to help the JIT
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "array", CD_Object.arrayType())
                      .invokevirtual(CD_Class, "cast", MethodTypeDesc.of(CD_Object, CD_Object))
                      .checkcast(CD_Object.arrayType())
                      .iload(1)
                      .aaload()
                      .areturn();
                }));
  }

  private static void generateTemplateSizeMethod(ClassBuilder classBuilder) {
    classBuilder.withMethod(
        "size", MethodTypeDesc.of(CD_int),
        ACC_PUBLIC,
        methodBuilder ->
            methodBuilder.withCode(codeBuilder -> {
                  codeBuilder
                      .aload(0)
                      .getfield(TEMPLATE_CLASS, "size", CD_int)
                      .ireturn();
                }));
  }

  private static void generateTemplateIsFlatMethod(ClassBuilder classBuilder) {
    classBuilder.withMethod(
        "isFlat", MethodTypeDesc.of(CD_boolean),
        ACC_PUBLIC,
        methodBuilder ->
            methodBuilder.withCode(codeBuilder -> {
              codeBuilder
                  .aload(0)
                  .getfield(TEMPLATE_CLASS, "array", CD_Object.arrayType())
                  .invokestatic(CD_VALUE_CLASS, "isFlatArray", MethodTypeDesc.of(CD_boolean, CD_Object))
                  .ireturn();
            }));
  }


  // Runtime

  // Extract the array type from a Class, called by the generated code
  private static Class<?> arrayType(MethodHandles.Lookup lookup, String name, Class<?> type, Specialization specialization) {
    return specialization.elementType.arrayType();
  }

  private static void checkFlat(Object[] array) {
    if (!ValueClass.isFlatArray(array)) {
      throw new IllegalStateException("array is not a flat array");
    }
  }

  // Directly called by the bytecode, must be inlined
  private static Object[] newArray(Specialization specialization, int capacity) {
    // do not use an expression switch here, the generated code is too awful :(
    switch (specialization.properties) {
      case NON_FLAT -> {
        return (Object[]) Array.newInstance(specialization.elementType, capacity);
      }
      case FLAT -> {
        return ValueClass.newNullableAtomicArray(specialization.elementType, capacity);
      }
      case NON_NULL_FLAT -> {
        return ValueClass.newNullRestrictedAtomicArray(specialization.elementType, capacity, specialization.defaultValue);
      }
      case NON_ATOMIC_FLAT -> {
        return ValueClass.newNullRestrictedNonAtomicArray(specialization.elementType, capacity, specialization.defaultValue);
      }
      default -> throw new IllegalArgumentException("Unknown properties: " + specialization.properties);
    }
  }

  // Directly called by the bytecode, slow path
  private static Object[] arrayResize(Specialization specialization, Object[] values) {
    var newCapacity = Math.max(16, values.length << 1);
    if (!ValueClass.isNullRestrictedArray(values)) {
      return Arrays.copyOf(values, newCapacity);
    }
    if (ValueClass.isAtomicArray(values)) {
      var newArray = ValueClass.newNullRestrictedAtomicArray(specialization.elementType, newCapacity, specialization.defaultValue);
      System.arraycopy(values, 0, newArray, 0, values.length);
      return newArray;
    }
    var newArray = ValueClass.newNullRestrictedNonAtomicArray(specialization.elementType, newCapacity, specialization.defaultValue);
    System.arraycopy(values, 0, newArray, 0, values.length);
    return newArray;
  }


  // Example usage and test
  // --enable-preview --add-exports=java.base/jdk.internal.value=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
  public static void main(String[] args) {
    // Create specialized lists for different types
    List<String> stringList = create(String.class, NON_FLAT);

    // Test string list
    stringList.add("Hello");
    stringList.add("World");
    stringList.add("Specialized");
    stringList.add("Arrays");

    System.out.println(stringList);

    // Test integer list
    List<Integer> intList = create(Integer.class, NON_NULL_FLAT);
    for(var i = 0; i < 20; i++) {
      intList.add(i);
    }

    System.out.println(intList);

    // Verify that different types have different hidden classes but share template
    List<String> anotherStringList = create(String.class, NON_FLAT);
    System.out.println(
        "String classes are same: " + (stringList.getClass() == anotherStringList.getClass()));
    System.out.println(
        "String and Integer classes are different: "
            + (stringList.getClass() != intList.getClass()));

    // Test methods inherited from AbstractList
    System.out.println("Contains 'Hello': " + stringList.contains("Hello"));
    System.out.println("Index of 'World': " + stringList.indexOf("World"));

    // Test that arrays are properly typed
    Object[] stringArray = stringList.toArray();
    System.out.println("String array type: " + stringArray.getClass().getComponentType());
  }
}
