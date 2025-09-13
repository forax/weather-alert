import jdk.internal.vm.annotation.NullRestricted;

value
record Data(
  @NullRestricted
  Boolean b1,

  @NullRestricted
  Boolean b2,

  @NullRestricted
  Integer i1,

  @NullRestricted
  Integer i2) {
}

// -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlineLayout
void main() {
  var data = new Data(true, false, 1, 2);
  IO.println(data);
}
