package com.weaverplatform.absexcelconverter.util.workbook;

import com.weaverplatform.absexcelconverter.util.workbook.model.ABSExcel;
import com.weaverplatform.protocol.WeaverError;

/**
 * Indicates wheter or not an Excel sheet is up to the norm for ABS elements.
 * 
 * @author alex
 *
 */
public interface Validator {

  /**
   * Checks wheter or not this document meets the set criteria for
   * particular Excel sheets.
   * 
   * @return true when it does, false otherwise.
   */
  boolean check();

  /**
   * Raw read a document and return all of it's contents, including empty
   * rows/columns.
   * 
   * @return a two dimensional array reflecting a document.
   */
  String[][] raw();

  /**
   * Pretty read and constructs an ABSExcel object which can be easily read.
   * 
   * @return an ABSExcel object containing all data provided in the excel
   *         document. Including empty rows and columns.
   * @throws a
   *           NullPointerException when no conversion could be made.
   */
  ABSExcel read() throws WeaverError;

}
