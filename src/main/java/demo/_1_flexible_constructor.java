
class Windspeed {
  private int value;

  public Windspeed(int value) {
    super();
    if (value < 0) {
      throw new IllegalArgumentException("value < 0");
    }
    // super();
    this.value = value;
    // super();
  }

  @Override
  public String toString() {
    return value + " km/h";
  }
}

void main() {
  var windspeed = new Windspeed(10);
  System.out.println(windspeed);
}
