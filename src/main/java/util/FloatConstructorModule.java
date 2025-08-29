package util;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// Deserialize all classes that have a constructor with a single float parameter
public final class FloatConstructorModule extends SimpleModule {
  public FloatConstructorModule() {
    setDeserializerModifier(new BeanDeserializerModifier() {
      @Override
      public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                                                    JsonDeserializer<?> deserializer) {
        var beanClass = beanDesc.getBeanClass();
        try {
          var constructor = beanClass.getDeclaredConstructor(float.class);
          constructor.setAccessible(true);
          return new DynamicFloatDeserializer(constructor);
        } catch (NoSuchMethodException _) {
          // ignore
        }
        return deserializer; // send default deserializer
      }
    });
  }

  private static final class DynamicFloatDeserializer extends JsonDeserializer<Object> {
    private final Constructor<?> constructor;

    private DynamicFloatDeserializer(Constructor<?> constructor) {
      this.constructor = constructor;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext context) throws IOException {
      var targetClass = constructor.getDeclaringClass();
      if (!p.getCurrentToken().isNumeric()) {
        throw context.wrongTokenException(p, targetClass, JsonToken.VALUE_NUMBER_FLOAT,
            "Expected numeric value for " + targetClass.getName());
      }
      var value = p.getFloatValue();
      try {
        return constructor.newInstance(value);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new IOException(
            "Failed to deserialize " + targetClass.getName() + " from numeric value", e);
      } catch (InvocationTargetException e) {
        var cause = e.getCause();
        if (cause instanceof Error error) {
          throw error;
        }
        throw new IOException(
            "Failed to deserialize " + targetClass.getName() + " from numeric value", cause);
      }
    }
  }
}
