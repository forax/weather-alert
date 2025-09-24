
record Data(
  Boolean b1,
  Boolean b2,
  Double d) {
}

// -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlineLayout
void main() {
  var data = new Data(true, false, 3.14);
  IO.println(data);
}
