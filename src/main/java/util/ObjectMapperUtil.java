package util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public final class ObjectMapperUtil {

  public static ObjectReader newBasicObjectReader() {
    return newBasicMapper().reader();
  }

  public static ObjectReader newTypeAwareObjectReader() {
    var module = new SimpleModule();
    module.addDeserializer(List.class, new TypeAwareListDeserializer(null));
    var mapper = newBasicMapper();
    mapper.registerModule(module);

    return mapper.reader();
  }

  private static ObjectMapper newBasicMapper() {
    var mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    SimpleModule module = new SimpleModule();
    // Identity
    module.addDeserializer(identity.weather.WeatherService.Temperature.class, floatRecordDeserializer(identity.weather.WeatherService.Temperature::new));
    module.addDeserializer(identity.weather.WeatherService.Windspeed.class, floatRecordDeserializer(identity.weather.WeatherService.Windspeed::new));
    module.addDeserializer(identity.weather.WeatherService.Precipitation.class, floatRecordDeserializer(identity.weather.WeatherService.Precipitation::new));

    // Value
    module.addDeserializer(value.weather.WeatherService.Temperature.class, floatRecordDeserializer(value.weather.WeatherService.Temperature::new));
    module.addDeserializer(value.weather.WeatherService.Windspeed.class, floatRecordDeserializer(value.weather.WeatherService.Windspeed::new));
    module.addDeserializer(value.weather.WeatherService.Precipitation.class, floatRecordDeserializer(value.weather.WeatherService.Precipitation::new));

    mapper.registerModule(module);

    return mapper;
  }

  private static <T> JsonDeserializer<T> floatRecordDeserializer(Function<Float, T> constructor) {
    return new JsonDeserializer<>() {
      @Override
      public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return constructor.apply((float) parser.getDoubleValue());
      }
    };
  }
}
