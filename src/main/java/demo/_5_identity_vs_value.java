/*no identity*/ value record Windspeed(int value) {
  public Windspeed {
    if (value < 0) {
      throw new IllegalArgumentException("value < 0");
    }
  }

  @Override
  public String toString() {
    return value + " km/h";
  }
}

void main() {
  Windspeed windspeed = new Windspeed(10);
  Windspeed windspeed2 = new Windspeed(10);

  // new semantics for ==
  IO.println(windspeed == windspeed2);

  // new semantics for identityHashCode
  IO.println(System.identityHashCode(windspeed));
  IO.println(System.identityHashCode(windspeed2));
  
  //synchronized (windspeed) { }

  Object o = windspeed;
  synchronized (o) { }
}
