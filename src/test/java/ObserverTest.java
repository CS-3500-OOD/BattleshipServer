import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import json.JsonUtils;
import json.ObserverJSON;

public class ObserverTest {


  public static void main(String[] args) {

    try {
      ServerSocket serverSocket = new ServerSocket(12345);
      System.out.println("Started observer...");
      Socket connection = serverSocket.accept();
      System.out.println("Accepted connection " + connection);
      JsonParser parser = new ObjectMapper().createParser(connection.getInputStream());

      while(!connection.isClosed()) {
        ObserverJSON json = parser.readValueAs(ObserverJSON.class);

        System.out.println("Received: ");
        System.out.println(JsonUtils.prettifyJSON(json));
        System.out.println("\n\n");
      }


    } catch (IOException e) {
      System.out.println("Exception: " + e);
    }


  }


}
