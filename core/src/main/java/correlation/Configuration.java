package correlation;

public class Configuration {

  static String zipkinURi() {
    return System.getProperty("c9n.zipkin");
  }
}
