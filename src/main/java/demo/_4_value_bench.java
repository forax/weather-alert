/*value*/ record Windspeed(float value) {
  public Windspeed min(Windspeed temperature) {
    return new Windspeed(Math.min(value, temperature.value));
  }

  public Windspeed max(Windspeed temperature) {
    return new Windspeed(Math.max(value, temperature.value));
  }
}

void main() {

  var speeds = new Windspeed[10_000];
  for (int i = 0; i < speeds.length; i++) {
    speeds[i] = new Windspeed(i);
  }

  var max = new Windspeed(Float.MIN_VALUE);
  var min = new Windspeed(Float.MAX_VALUE);

  for (int i = 0; i < 10_000; i++) {
    var speed = speeds[i];
    max = max.max(speed);
    min = min.min(speed);
  }
}
