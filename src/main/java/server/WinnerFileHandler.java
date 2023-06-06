package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public class WinnerFileHandler {

  public static boolean saveWinners(Set<String> winners, String outFilePath) {

    try {
      StringBuilder builder = new StringBuilder();
      for(String winner : winners) {
        builder.append(winner).append("\n");
      }

      Files.writeString(new File(outFilePath).toPath(), builder.toString(), StandardOpenOption.CREATE);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
