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
  var windspeed = new Windspeed(10);
  var windspeed2 = new Windspeed(10);

  // new semantics for == must be defined !
  System.out.println(windspeed == windspeed2);

  // new semantics for identityHashCode must be defined !
  System.out.println(System.identityHashCode(windspeed));
  System.out.println(System.identityHashCode(windspeed2));

  // new semantics for synchronized must be defined !
  //synchronized (windspeed) { }

  Object o = windspeed;
  synchronized (o) { }
}
