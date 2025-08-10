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

The primitive version is just for performance comparison.


## Key Differences Between Identity and Value Classes

### **Memory Management and Performance**

**Identity Classes:**
- Objects are allocated on the heap with traditional `new` keyword
- Each object has its own identity and memory location
- Reference-based equality (objects are equal only if they reference the location in memory)
- Pointer indirection when accessing nested objects

**Value Classes:**
- No object identity! Equality is based on content/values
- Unmodifiability (there is no location in memory, thus the object is unmodifiable)
- Direct memory access without pointer indirection
- Reduced garbage collection pressure


No identity enables two optimizations:
- **Scalarization**: Instances don't use heap memory when allocated with `new` (but CPU registers)
- **Heap Flattening**: Fields of value classes are embedded directly inside container classes

The value classes tries to be the best of both worlds (primitive types and identity classes).


### **Declaration Syntax**

**Identity Classes:**
```java
public class WeatherData {
    // Traditional class declaration
}
```


**Value Classes:**

A value class is declared with the `value` keyword.

```java
public value class WeatherData {
    // Uses 'value' keyword
}
// or
public value record WeatherData(...) {
    // Can also be applied to records
}
```

At runtime, there are very few differences:
- The class is implicitly marked as `final`
- The fields are implicitly marked as `final` and `strict` (initialized before the call to the super constructor)
- the operator == checks the field values (component wise) instead of the reference
- System.identityHashCode() returns a hash computed using the field values
- synchonized(value) throw an IdentityException because there is no memory location associated to the object.
- new WeakReference(value) throws an IdentityException because the instance is not accessible through a pointer.


## Different use cases

### **Builder Pattern Implementation**

**Identity Classes (Imperative Builders):**
- More complex due to mutation and state tracking
- Need to manage intermediate states
- Risk of code duplication between constructor and setters (withers)

see [QueryBuilder](src/main/java/identity/weather/QueryBuilder.java)

**Value Classes (Functional Builders):**
- Simpler implementation due to immutability
- No state tracking needed
- Safer with no code duplication
- Purely functional approach

see [ValueQueryBuilder](src/main/java/value/weather/ValueQueryBuilder.java)


### **Computational Code Benefits**

**Identity Classes:**
- Performance considerations may discourage grouping related fields
- More verbose code due to manual optimization concerns
- Potential for scattered data structures

see methods `computeWeatherData()` and `computeHourlyData()` in
[WeatherComputation](src/main/java/identity/weather/WeatherComputation.java)

**Value Classes:**
- Can group fields freely without performance penalties
- More readable and reusable computation code
- Natural data modeling without artificial performance constraints
- Methods like `computeWeatherData()` and `computeHourlyData()` become cleaner

see methods `computeWeatherData()` and `computeHourlyData()` in
[WeatherComputation](src/main/java/value/weather/WeatherComputation.java)


### **Collection Behavior Limitations**

**Both Approaches Share This Limitation:**
- Generic collections (like `java.util.List<T>`) don't benefit from value class optimizations
- Type erasure removes runtime type information
- No flattening or performance improvements in generic containers

To get better performance, need specialized collections:
- Requires special handling (like [TypeAwareListDeserializer](src/main/java/util/TypeAwareListDeserializer.java) for Jackson)
- Must replace `Stream.toList()` with `collect(Collectors.toCollection())` for better control,
  see [WeatherComputation.toWeatherData()](src/main/java/identity/weather/WeatherComputation.java)


## **Development Philosophy**

**Identity Classes:**
- Focus on object identity and reference semantics
- Traditional OOP approach
- Performance optimizations require careful design considerations
- Well-established patterns and tooling

**Value Classes:**
- Focus on data and values rather than identity
- Functional programming friendly
- Performance comes naturally through JVM optimizations
- Experimental feature requiring cutting-edge version of Java and IDE support


## **Using IntelliJ IDEA**

Recent versions of IntelliJ have partial support for value types.
In File > Project Structure, select the "Language Level" to "Experimental Features"
and for the Module "weather-alert" select either "Experimental Features" or "Project default".
