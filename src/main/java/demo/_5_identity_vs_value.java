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

/*abstract*/ value class Point2D {
  private float x;
  private float y;

  public Point2D(float x, float y) {
    this.x = x;
    this.y = y;
  }
}

//value class Point3D extends Point2D {
//  private float z;
//
//  Point3D(float x, float y, float z) {
//    super(x, y);
//    this.z = z;
//  }
//}

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
