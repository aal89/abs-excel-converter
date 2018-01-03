package com.weaverplatform.absexcelconverter.util;

import static spark.Spark.before;
import static spark.Spark.options;

/**
 * Web requests often require CORS headers to be present
 * so that cross domain requests can be executed. This
 * class will deliver this functionality to Spark, it is
 * designed to be middleware that needs activation.
 *
 * @author alex
 *
 */
public class CORS {

	private static boolean enabled = false;

	// Enables CORS on requests. This method is an initialization method and should be called once.
	public static void enable() {
		if(enabled) {
      return;
    }

		enabled = true;

		options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");

      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
		});

		before((request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Request-Method", "*");
      response.header("Access-Control-Allow-Headers", "*");
      response.type("application/json");
		});
	}
}
