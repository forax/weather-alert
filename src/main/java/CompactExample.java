import jdk.internal.vm.annotation.NullRestricted;

value record Data(Integer i, byte b, @NullRestricted Long l) { }

// --enable-preview
// -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlineLayout
// -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders
static void main(String[] args) {
  System.out.println(new Data(42, (byte) 10, -333L));
}
