package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class WhitelistFileHandler {

  public static boolean loadNewWhitelist(GamesManager manager, String inputFilePath) {
    try {
      Set<String> whitelist = new HashSet<>(Files.readAllLines(new File(inputFilePath).toPath()));
      manager.enableWhitelist(whitelist);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

}
