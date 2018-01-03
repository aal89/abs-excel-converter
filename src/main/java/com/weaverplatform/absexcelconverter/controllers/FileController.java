package com.weaverplatform.absexcelconverter.controllers;

import com.weaverplatform.absexcelconverter.util.File;
import com.weaverplatform.absexcelconverter.util.wo.WriteOperationParser;
import com.weaverplatform.absexcelconverter.util.workbook.Workbook;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 *
 * @author alex
 *
 */
public class FileController {

  /**
   * Upload route to upload excel files to convert into Weaver WriteOperations.
   */
  public static Route upload = (Request req, Response res) -> {
    Workbook workbook = new Workbook(File.get(req, false));
    return WriteOperationParser.parseArray(workbook);
  };
}
