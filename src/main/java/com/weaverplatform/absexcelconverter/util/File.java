package com.weaverplatform.absexcelconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import spark.Request;

/**
 * A class with static methods used to extract files from
 * http requests.
 * @author alex
 *
 */
public class File {
  
  public static long MAX_FILE_SIZE, MAX_REQUEST_SIZE;
  public static int FILE_SIZE_THRESHOLD;
  public static String LOCATION;
  
  static {
    MAX_FILE_SIZE           = Long.parseLong(Props.get("file.max_file_size"));    // the maximum size allowed for uploaded files
    MAX_REQUEST_SIZE        = Long.parseLong(Props.get("file.max_request_size")); // the maximum size allowed for multipart/form-data requests
    FILE_SIZE_THRESHOLD     = Integer.parseInt(Props.get("file.file_size_threshold"));     // the size threshold after which files will be written to disk
    LOCATION                = Props.get("file.location");                         // default folder to save data to, if desired
  }
  
  /**
   * Retrieves a Part named 'file' from a given Spark Request onbject.
   * @param request a Spark Request object.
   * @return a Part.
   * @throws ServletException 
   * @throws IOException 
   */
  public static Part get(Request req, boolean save) throws IOException, ServletException {
    MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
         LOCATION, MAX_FILE_SIZE, MAX_REQUEST_SIZE, FILE_SIZE_THRESHOLD);
    req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
    
    Part uploadedFile = req.raw().getPart("file");
    
    // This is here for debug purposes will be gone in non SNAPSHOT
    // versions
    if(save) {
      String fName = uploadedFile.getSubmittedFileName();
      Path out = Paths.get(LOCATION + "/" + fName);
      try (final InputStream in = uploadedFile.getInputStream()) {
         Files.copy(in, out);
         uploadedFile.delete();
      }
    }
    
    return uploadedFile;
  }

}
