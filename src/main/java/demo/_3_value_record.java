
value record Windspeed(int value) {
  public Windspeed {
    if (value < 0) {
      throw new IllegalArgumentException("value < 0");
    }
    //super();   // IntelliJ is wrong here !
  }

  @Override
  public String toString() {
    return value + " km/h";
  }
}

void main() {
  var windspeed = new Windspeed(10);
  System.out.println(windspeed);
  System.out.println(windspeed.getClass().isValue());
}
