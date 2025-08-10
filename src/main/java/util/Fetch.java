package util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Fetch {
  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();

  public static String fetch(URI uri) throws IOException {
    try {
      var request = HttpRequest.newBuilder().uri(uri).GET().build();
      var httpResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
      if (httpResponse.statusCode() != 200) {
        throw new IOException("API request failed with status code: " + httpResponse.statusCode() + "  " + httpResponse.body());
      }
      return httpResponse.body();
    } catch (InterruptedException e) {
      throw (InterruptedIOException) new InterruptedIOException().initCause(e);
    }
  }

  private static final Path CACHE_DIR = Paths.get("cache");
  static {
    try {
      Files.createDirectories(CACHE_DIR);
    } catch (IOException e) {
      // do nothing, the cache is just disable
    }
  }

  private static Path cachePath(URI uri) {
    return CACHE_DIR.resolve(uri.getQuery());
  }

  private static String readFromCache(URI uri) throws IOException {
    return Files.readString(cachePath(uri));
  }

  private static void storeIntoCache(URI uri, String json) throws IOException {
    Files.writeString(cachePath(uri), json);
  }

  @FunctionalInterface
  public interface IOFunction {
    String apply(URI uri) throws IOException;
  }

  public static String cache(URI uri, IOFunction function) throws IOException {
    try {
      return readFromCache(uri);
    } catch (IOException e) {
      var body = function.apply(uri);
      storeIntoCache(uri, body);
      return body;
    }
  }
}
