package com.dcos.utilities.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Fixtures {
  public static String getJSONFromFile(String filePath) throws IOException,URISyntaxException{
      return new String(Files.readAllBytes(Paths.get(Fixtures.class.getResource("/fixtures/" + filePath).toURI())));
  }
}
