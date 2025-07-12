package com.peti.backend;

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceLoader {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OBJECT_MAPPER.configure(Feature.ALLOW_COMMENTS, true);
    OBJECT_MAPPER.findAndRegisterModules();
  }

  /**
   * Prevents instantiation of this utility class.
   */
  private ResourceLoader() {

  }

  public static <T> T loadResource(String fileName, Class<T> type) {
    try {
      byte[] json = loadResourceBytes(fileName);
      return OBJECT_MAPPER.readValue(json, type);
    } catch (Exception e) {
      return fail(e);
    }
  }

  public static <T> T loadResource(String fileName, TypeReference<T> type) {
    try {
      byte[] json = loadResourceBytes(fileName);
      return OBJECT_MAPPER.readValue(json, type);
    } catch (Exception e) {
      return fail(e);
    }
  }

  public static byte[] loadResourceBytes(String fileName) {
    try {
      return Files.readAllBytes(pathOfResource(fileName));
    } catch (Exception e) {
      return fail(e);
    }
  }

  private static Path pathOfResource(String resourceName) throws URISyntaxException {
    URL resource = ResourceLoader.class.getClassLoader().getResource(resourceName);
    return Paths.get(resource.toURI());
  }

}
