# weather-alert
A repository demonstrating how to use value types on a real example

There are 3 versions:
1. identity (good old) classes
   - [identity WeatherExample](src/main/java/identity/weather/WeatherExample.java)
   - [identity WeatherService](src/main/java/identity/weather/WeatherService.java)
   - [identity WeatherComputation](src/main/java/identity/weather/WeatherComputation.java)

2. primitive types (just for performance)
   - [primitive WeatherExample](src/main/java/primitive/weather/WeatherExample.java)
   - [primitive WeatherService](src/main/java/primitive/weather/WeatherService.java) 
   - [primitive WeatherComputation](src/main/java/primitive/weather/WeatherComputation.java)
  
3. value classes (should get the best of both world)
   - [value WeatherExample](src/main/java/value/weather/WeatherExample.java)
   - [value WeatherService](src/main/java/value/weather/WeatherService.java)
   - [value WeatherComputation](src/main/java/value/weather/WeatherComputation.java)


### Declaring a simple value class

You can use the keyword `value` in front of a `class` or a `record`,
see [WeatherService](src/main/java/value/weather/WeatherService.java).

Allocating an instance (with `new`) of a value class does not use memory on the heap (scalarization).
Declaring a field typed by a value class embeds the values of the fields of the value class inside the container class
(heap flattening).


### Builder and Value Builder

Value classes let you right functional builders that are
- less complex (no mutation, having to track states)
- safer (no code duplication between the constructeur and the setter/with)

You can contrast [imperative QueryBuilder](src/main/java/identity/weather/QueryBuilder.java) vs
[functional QueryBuilder](src/main/java/value/weather/QueryBuilder.java).


### Computation

Value classes make the code that does computation
- more readable
- easier to reuse
because you can group fields with no fear of performance hiccup.

You can contrast the method `computeWeatherData()` or `computeHourlyData()`
in [identity based WeatherComputation.java](src/main/java/identity/weather/WeatherComputation.java)
and [value based](src/main/java/value/weather/WeatherComputation.java).


### Collection erasure vs value class

All is not rosy in the value world, a `java.util.List` of a value class behave exactly the same way
a list of identity class, no better performance, no flattening. It's because the type argument
of generic types (parametrized types) is removed by the compiler (erasure),
thus not present at runtime. A `java.util.List` does not know at runtime the class of its elements.

Sadly it means that we have to propagate the type at runtime, one way or another.
For Jackson, we need a deserializer which propagate the type argument,
see [TypeAwareListDeserializer](src/main/java/util/TypeAwareListDeserializer.java).
For the method `toList()` of a Stream, we need to replace it with `collect(Collectors.toCollection())`
so as a user, you control how the collection is initialized,
see the method [WeatherComputation.toWeatherData()](src/main/java/identity/weather/WeatherComputation.java).


## Using IntelliJ IDEA

Recent versions of IntelliJ have partial support for value types.
In File > Project Structure, select the "Language Level" to "Experimental Features"
and for the Module "weather-alert" select either "Experimental Features" or "Project default".
