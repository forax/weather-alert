package util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TypeAwareListDeserializer extends StdDeserializer<List<?>> implements ContextualDeserializer {
  public TypeAwareListDeserializer(JavaType type) {
    super(type);
  }

  private static List<Object> createList(JavaType contentType) {
    var rawClass = contentType.getRawClass();
    if (rawClass.isValue()) {
      //return new GenericValueList<>(rawClass);
      return ValueList.create(rawClass);
    }
    return new ArrayList<>();
  }

  @Override
  public List<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    var targetType = getValueType();
    var contentType = targetType.getContentType();

    var list = createList(contentType);
    if (p.isExpectedStartArrayToken()) {
      var elementDeserializer = ctxt.findRootValueDeserializer(contentType);
      JsonToken token;
      while ((token = p.nextToken()) != JsonToken.END_ARRAY) {
        Object element;
        if (token == JsonToken.VALUE_NULL) {
          element = elementDeserializer.getNullValue(ctxt);
        } else {
          element = elementDeserializer.deserialize(p, ctxt);
        }
        list.add(element);
      }
    }
    return list;
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
    return new TypeAwareListDeserializer(property.getType());
  }
}