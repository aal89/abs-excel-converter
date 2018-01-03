package com.weaverplatform.absexcelconverter;

import static spark.Spark.awaitInitialization;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.threadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weaverplatform.absexcelconverter.controllers.ApplicationController;
import com.weaverplatform.absexcelconverter.controllers.FileController;
import com.weaverplatform.absexcelconverter.util.CORS;
import com.weaverplatform.absexcelconverter.util.Props;
import com.weaverplatform.protocol.WeaverError;

/**
 * The main Application class in which all processes are bootstrapped and
 * initialized. Default properties are loaded, Spark routing controllers are
 * initialised and mapped to their respective routes. Spark itself gets setup
 * and started.
 *
 * @author alex
 *
 */
public class Application {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  public Application() {
    // Read properties
    final String NAME     = Props.get("application.name");
    final String VERSION  = Props.get("application.version");
    final int PORT        = Props.getInt("PORT", "application.port");

    // Port for Spark to listen on
    port(PORT);

    // Setup thread pool
    threadPool(100, 5, 30000);

    // For all requests, enable cross origin headers
    CORS.enable();

    // Route registration and mapping
    get("/",                  ApplicationController.about);
    get("/about",             ApplicationController.about);
    get("/connection",        ApplicationController.connection);
    
    post("/upload",           FileController.upload);
    
    get("*",                  ApplicationController.notFound);

    // Wait for server initialization
    awaitInitialization();

    // Catch WeaverError exceptions
    exception(WeaverError.class, (e, request, response) -> {
      response.status(400);
      response.body(e.toJson());
    });

    // Catch all other exceptions
    exception(Exception.class, (e, request, response) -> {
      logger.error("Server Error", e);
      response.status(503);
      response.body("503 - Server Error");
    });

    // Running
    logger.info("Running " + NAME + " " + VERSION + " on port " + PORT);
  }

  public static void main(String[] args) {
    new Application();
  }
}
