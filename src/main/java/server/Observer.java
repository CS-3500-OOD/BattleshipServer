package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import game.CellStatus;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import json.JsonUtils;
import json.ObserverJSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Observer {

  private static final Logger logger = LogManager.getLogger(Observer.class);

  private boolean continueConnection = true;

  private static final String PASSWORD = System.getenv("BS_MONGO_PASS");

  // old: "mongodb+srv://lucasharbani:YogQ6VJo0vTUaqSM@cs3500-cluster.mbwz0vd.mongodb.net/?retryWrites=true&w=majority"
  private static final String MONGO_URI = "mongodb+srv://nick:" + PASSWORD + "@cs3500-paid-cluster.4nsyu.mongodb.net/?retryWrites=true&w=majority";
  private static final String MONGO_DATABASE = "BattleSalvo";
  private static final String MONGO_COLLECTION = "rounds";

  private MongoCollection<Document> collection;

  public Observer() {
    try {
      MongoClient mongoClient = MongoClients.create(MONGO_URI);
      MongoDatabase mongoDatabase = mongoClient.getDatabase(MONGO_DATABASE);
      this.collection = mongoDatabase.getCollection(MONGO_COLLECTION);
      logger.info("Observer online.");
    }
    catch (IllegalArgumentException e) {
      logger.error("Unable to setup Observer");
      this.continueConnection = false;
    }
  }

  // Testing purposes only...
  public static void main(String[] args) {
    Observer observer = new Observer();
//    for(Document d : observer.collection.find()) {
//      System.out.println(d);
//    }
    observer.collection.insertOne(new Document());
  }

  public synchronized void updateObserver(ObserverJSON observerJSON) {
    if(this.continueConnection) {
      try {
        JsonNode node = JsonUtils.serializeRecordToJson(observerJSON);
        Document doc = Document.parse(node.toString());
        collection.insertOne(doc);
      }
      catch (Exception e) {
        logger.error("Unable to update observer: " + e);
        this.continueConnection = false;
      }
    }
  }


  public boolean isConnected() {
    return this.continueConnection;
  }

  public void stopObserver() {
    this.continueConnection = false;
  }

}
