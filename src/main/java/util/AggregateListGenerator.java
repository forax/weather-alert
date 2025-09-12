package util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;

import static java.lang.classfile.ClassFile.*;
import static java.lang.classfile.ClassFile.JAVA_25_VERSION;
import static java.lang.constant.ConstantDescs.*;
import static java.lang.invoke.MethodHandles.Lookup.ClassOption.NESTMATE;
import static java.lang.invoke.MethodHandles.Lookup.ClassOption.STRONG;

final class AggregateListGenerator {
  private AggregateListGenerator() {
    throw new AssertionError();
  }

  /* model
  public final class AggregateListImpl implements AggregateList<Tuple> {
    private final List<?> list0;
    private final List<?> list1;

    public AggregateListImpl(List<?> list0, List<?> list1) {
      this.list0 = list0;
      this.list1 = list1;
    }

    @Override
    public Tuple get(int index) {
      return new Tuple(list0.get(index), list1.get(index));
    }

    @Override
    public Tuple set(int index, Tuple element) {
      var old = get(index);
      list0.set(index, tuple.left);
      list1.set(index, tuple.right);
      return old;
    }

    @Override
    public int size() {
      return list0.size();
    }

    @Override
    public boolean equals(Object obj) {
      return defaultEquals(this, obj);
    }
    @Override
    public int hashCode() {
      return defaultHashCode(this);
    }
    @Override
    public String toString() {
      return defaultToString(this);
    }
  }*/


  private static final ClassDesc CD_AGGREGATE_LIST = ClassDesc.of(AggregateList.class.getName());

  private static byte[] generateAggregateListImpl(Class<?> lookupClass, Class<?> recordType, RecordComponent[] components) {;
    var thisClass = ClassDesc.of(lookupClass.getPackageName(), "AggregateListImpl");
    return ClassFile.of().build(thisClass, cb -> {
      // Class modifiers and extends/implements
      cb.withVersion(JAVA_26_VERSION, PREVIEW_MINOR_VERSION);
      cb.withFlags(ACC_PUBLIC | ACC_FINAL);
      cb.withSuperclass(CD_Object);
      cb.withInterfaceSymbols(CD_AGGREGATE_LIST);

      // Generate fields (list0, list1, etc.)
      for (var i = 0; i < components.length; i++) {
        var fieldName = "list" + i;
        cb.withField(fieldName, CD_List, fb -> {
          fb.withFlags(ACC_PRIVATE | ACC_FINAL | ACC_STRICT);
        });
      }

      var recordDesc = ClassDesc.of(recordType.getName());
      generateConstructor(cb, thisClass, components);
      generateGetMethod(cb, thisClass, recordDesc, components);
      generateSetMethod(cb, thisClass, recordDesc, components);
      generateSizeMethod(cb, thisClass);

      generateEqualsMethod(cb);
      generateHashCodeMethod(cb);
      generateToStringMethod(cb);
    });
  }

  private static void generateConstructor(ClassBuilder cb, ClassDesc thisClass, RecordComponent[] components) {
    var constructorDesc = MethodTypeDesc.of(CD_void,
        Collections.nCopies(components.length, CD_List));
    cb.withMethod(INIT_NAME, constructorDesc, ACC_PUBLIC, mb -> {
      mb.withCode(codeb -> {
        // Initialize fields
        for (int i = 0; i < components.length; i++) {
          var fieldName = "list" + i;
          codeb.aload(0);  // this
          codeb.aload(i + 1);  // parameter
          codeb.putfield(thisClass, fieldName, CD_List);
        }

        // Call super()
        codeb.aload(0);
        codeb.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void));

        codeb.return_();
      });
    });
  }

  private static void unbox(CodeBuilder codeb, Class<?> componentType) {
    if (componentType.isPrimitive()) {
      switch(componentType.getName()) {
        case "boolean" -> {
          codeb.checkcast(CD_Boolean);
          codeb.invokevirtual(CD_Boolean, "booleanValue", MethodTypeDesc.of(CD_boolean));
        }
        case "byte" -> {
         codeb.checkcast(CD_Byte);
         codeb.invokevirtual(CD_Byte, "byteValue", MethodTypeDesc.of(CD_byte));
        }
        case "char" -> {
          codeb.checkcast(CD_Character);
          codeb.invokevirtual(CD_Character, "charValue", MethodTypeDesc.of(CD_char));
        }
        case "short" -> {
          codeb.checkcast(CD_Short);
          codeb.invokevirtual(CD_Short, "shortValue", MethodTypeDesc.of(CD_short));
        }
        case "int" -> {
          codeb.checkcast(CD_Integer);
          codeb.invokevirtual(CD_Integer, "intValue", MethodTypeDesc.of(CD_int));
        }
        case "long" -> {
          codeb.checkcast(CD_Long);
          codeb.invokevirtual(CD_Long, "longValue", MethodTypeDesc.of(CD_long));
        }
        case "float" -> {
          codeb.checkcast(CD_Float);
          codeb.invokevirtual(CD_Float, "floatValue", MethodTypeDesc.of(CD_float));
        }
        case "double" -> {
          codeb.checkcast(CD_Double);
          codeb.invokevirtual(CD_Double, "doubleValue", MethodTypeDesc.of(CD_double));
        }
        default ->
          throw new AssertionError("unexpected primitive type: " + componentType);
      }
      return;
    }
    codeb.checkcast(ClassDesc.ofDescriptor(componentType.descriptorString()));
  }

  private static void box(CodeBuilder codeb, Class<?> componentType) {
    if (componentType.isPrimitive()) {
      switch (componentType.getName()) {
        case "boolean" ->
            codeb.invokestatic(CD_Boolean, "valueOf", MethodTypeDesc.of(CD_Boolean, CD_boolean));
        case "byte" ->
            codeb.invokestatic(CD_Byte, "valueOf", MethodTypeDesc.of(CD_Byte, CD_byte));
        case "char" ->
            codeb.invokestatic(CD_Character, "valueOf", MethodTypeDesc.of(CD_Character, CD_char));
        case "short" ->
            codeb.invokestatic(CD_Short, "valueOf", MethodTypeDesc.of(CD_Short, CD_short));
        case "int" ->
            codeb.invokestatic(CD_Integer, "valueOf", MethodTypeDesc.of(CD_Integer, CD_int));
        case "long" ->
            codeb.invokestatic(CD_Long, "valueOf", MethodTypeDesc.of(CD_Long, CD_long));
        case "float" ->
            codeb.invokestatic(CD_Float, "valueOf", MethodTypeDesc.of(CD_Float, CD_float));
        case "double" ->
            codeb.invokestatic(CD_Double, "valueOf", MethodTypeDesc.of(CD_Double, CD_double));
        default -> throw new AssertionError("unexpected primitive type: " + componentType);
      }
    }
  }

  private static void generateGetMethod(ClassBuilder cb, ClassDesc thisClass, ClassDesc recordDesc, RecordComponent[] components) {
    var getDesc = MethodTypeDesc.of(CD_Object, CD_int);
    cb.withMethod("get", getDesc, ACC_PUBLIC, mb -> {
      mb.withCode(codeb -> {
        // Create a new record instance
        codeb.new_(recordDesc);
        codeb.dup();

        // Load arguments for the record constructor
        for (var i = 0; i < components.length; i++) {
          var fieldName = "list" + i;
          codeb.aload(0);  // this
          codeb.getfield(thisClass, fieldName, CD_List);
          codeb.iload(1);  // index parameter
          codeb.invokeinterface(CD_List, "get",
              MethodTypeDesc.of(CD_Object, CD_int));
          unbox(codeb, components[i].getType());
        }

        // Call record constructor
        var parameterTypes = new ClassDesc[components.length];
        for (var i = 0; i < components.length; i++) {
          parameterTypes[i] = ClassDesc.ofDescriptor(components[i].getType().descriptorString());
        }
        codeb.invokespecial(recordDesc, INIT_NAME,
            MethodTypeDesc.of(CD_void, parameterTypes));

        codeb.areturn();
      });
    });
  }

  private static void generateSetMethod(ClassBuilder cb, ClassDesc thisClass, ClassDesc recordDesc, RecordComponent[] components) {
    var setDesc = MethodTypeDesc.of(CD_Object, CD_int, CD_Object);
    cb.withMethod("set", setDesc, ACC_PUBLIC, mb -> {
      mb.withCode(codeb -> {
        // var old = get(index);
        codeb.aload(0);  // this
        codeb.iload(1);  // index
        codeb.invokevirtual(thisClass, "get",
            MethodTypeDesc.of(CD_Object, CD_int));
        codeb.checkcast(recordDesc);
        codeb.astore(3);  // store in local variable

        // Set each component in the corresponding list
        for (var i = 0; i < components.length; i++) {
          codeb.aload(0);  // this
          codeb.getfield(thisClass, "list" + i, CD_List);
          codeb.iload(1);  // index
          codeb.aload(2);  // element parameter
          codeb.checkcast(recordDesc);
          codeb.invokevirtual(recordDesc, components[i].getName(),
              MethodTypeDesc.of(ClassDesc.ofDescriptor(components[i].getType().descriptorString())));  // call record accessor
          box(codeb, components[i].getType());
          codeb.invokeinterface(CD_List, "set",
              MethodTypeDesc.of(CD_Object, CD_int, CD_Object));
          codeb.pop();  // discard return value
        }

        // return old;
        codeb.aload(3);
        codeb.areturn();
      });
    });
  }

  private static void generateSizeMethod(ClassBuilder cb, ClassDesc thisClass) {
    cb.withMethod("size", MethodTypeDesc.of(CD_int), ACC_PUBLIC, mb -> {
      mb.withCode(codeb -> {
        codeb.aload(0);  // this
        codeb.getfield(thisClass, "list0", CD_List);
        codeb.invokeinterface(CD_List, "size", MethodTypeDesc.of(CD_int));
        codeb.ireturn();
      });
    });
  }

  private static void generateEqualsMethod(ClassBuilder cb) {
    cb.withMethod("equals", MethodTypeDesc.of(CD_boolean, CD_Object), ACC_PUBLIC, mb -> {
      mb.withCode(codeb -> {
        codeb.aload(0);  // this
        codeb.aload(1);  // obj parameter
        codeb.invokestatic(CD_AGGREGATE_LIST, "defaultEquals",
            MethodTypeDesc.of(CD_boolean, CD_AGGREGATE_LIST, CD_Object), true);
        codeb.ireturn();
      });
    });
  }

  private static void generateHashCodeMethod(ClassBuilder cb) {
    cb.withMethod("hashCode", MethodTypeDesc.of(CD_int), ACC_PUBLIC, mb -> {
      mb.withCode(codeb -> {
        codeb.aload(0);  // this
        codeb.invokestatic(CD_AGGREGATE_LIST, "defaultHashCode",
            MethodTypeDesc.of(CD_int, CD_AGGREGATE_LIST), true);
        codeb.ireturn();
      });
    });
  }

  private static void generateToStringMethod(ClassBuilder cb) {
    cb.withMethod("toString", MethodTypeDesc.of(CD_String), ACC_PUBLIC, mb -> {
      mb.withCode(codeb -> {
        codeb.aload(0);  // this
        codeb.invokestatic(CD_AGGREGATE_LIST, "defaultToString",
            MethodTypeDesc.of(CD_String, CD_AGGREGATE_LIST), true);
        codeb.areturn();
      });
    });
  }



  static <E> AggregateList.Factory<E> factory(MethodHandles.Lookup lookup, Class<E> recordType) {
    record AggregateFactoryImpl<E>(MethodHandle mh) implements AggregateList.Factory<E> {
      @Override
      @SuppressWarnings("unchecked")
      public AggregateList<E> create(List<?> list0) {
        Objects.requireNonNull(list0);
        try {
          return (AggregateList<E>) mh.invokeExact(list0);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public AggregateList<E> create(List<?> list0, List<?> list1) {
        checkListSize(list0, list1);
        try {
          return (AggregateList<E>) mh.invokeExact(list0, list1);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2) {
        checkListSize(list0, list1, list2);
        try {
          return (AggregateList<E>) mh.invokeExact(list0, list1, list2);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3) {
        checkListSize(list0, list1, list2, list3);
        try {
          return (AggregateList<E>) mh.invokeExact(list0, list1, list2, list3);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3, List<?> list4) {
        checkListSize(list0, list1, list2, list3, list4);
        try {
          return (AggregateList<E>) mh.invokeExact(list0, list1, list2, list3, list4);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3, List<?> list4, List<?> list5) {
        checkListSize(list0, list1, list2, list3, list4, list5);
        try {
          return (AggregateList<E>) mh.invokeExact(list0, list1, list2, list3, list4, list5);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3, List<?> list4, List<?> list5, List<?> list6) {
        checkListSize(list0, list1, list2, list3, list4, list5, list6);
        try {
          return (AggregateList<E>) mh.invokeExact(list0, list1, list2, list3, list4, list5, list6);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public AggregateList<E> create(List<?> list0, List<?> list1, List<?> list2, List<?> list3, List<?> list4, List<?> list5, List<?> list6, List<?> list7) {
        checkListSize(list0, list1, list2, list3, list4, list5, list6, list7);
        try {
          return (AggregateList<E>) mh.invokeExact(list0, list1, list2, list3, list4, list5, list6, list7);
        } catch (RuntimeException | Error e) {
          throw e;
        } catch (Throwable e) {
          throw new UndeclaredThrowableException(e);
        }
      }
    }

    var mh = createMH(lookup, recordType);
    return new AggregateFactoryImpl<>(mh);
  }

  private static void checkListSize(List<?>... lists) {
    var list0 = Objects.requireNonNull(lists[0], "list0 must not be null");
    for (var i = 1; i < lists.length; i++) {
      var list = lists[i];
      if (list == null) {
        throw new NullPointerException("list" + i + " must not be null");
      }
      if ( list0.size() != list.size()) {
        throw new IllegalArgumentException("lists must be the same size");
      }
    }
  }

  private static MethodHandle createMH(MethodHandles.Lookup lookup, Class<?> recordType) {
    var components = recordType.getRecordComponents();
    var classBytes = generateAggregateListImpl(lookup.lookupClass(), recordType, components);

    // DEBUG
    /*ClassFile.of().parse(classBytes).methods().forEach(method -> {
      System.err.println("Method: " + method.methodName().stringValue());
      method.findAttribute(Attributes.code()).ifPresent(code -> {
        code.elementList()
            .forEach(element -> System.err.println("  " + element));
      });
    });*/

    MethodHandles.Lookup implLookup;
    try {
      implLookup = lookup.defineHiddenClass(classBytes, true, NESTMATE, STRONG);
    } catch (IllegalAccessException e) {
      throw (IllegalAccessError) new IllegalAccessError().initCause(e);
    }
    var methodType = MethodType.methodType(void.class, Collections.nCopies(components.length, List.class));
    MethodHandle constructor;
    try {
      constructor = implLookup.findConstructor(implLookup.lookupClass(), methodType);
    } catch (NoSuchMethodException e) {
      throw (NoSuchMethodError) new NoSuchMethodError().initCause(e);
    } catch (IllegalAccessException e) {
      throw (IllegalAccessError) new IllegalAccessError().initCause(e);
    }
    return constructor.asType(methodType.changeReturnType(AggregateList.class));
  }

  // Example usage
  public static void main(String[] args) throws Throwable {
    // Assuming you have a Tuple record defined somewhere
    record Tuple(int left, String right) {}

    // Generate the class
    var tupleClass = Tuple.class;
    var factory = factory(MethodHandles.lookup(), tupleClass);

    var list = factory.create(List.of(42), List.of("foo"));
    System.out.println("value class ? " + list.getClass().isValue());
    System.out.println(list);
  }
}
