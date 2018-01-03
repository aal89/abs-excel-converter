package com.weaverplatform.absexcelconverter.util.workbook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A model containing all the data provided in an ABS Excel sheet. Containing
 * methods and enumarations to iterate over the provided data.
 * 
 * An ABSExcel sheet fundamentally is a two dimensional table with a header row.
 * In this table data is stored to generate Weaver WriteOperation. This object
 * should be used like prepared statements are used. For example if you want to
 * retrieve row 2 from the sheet, you call getRow(1), ignoring the zeroth row,
 * which is the header row. To make this example more concrete, consider the
 * attachment in this issue: http://jira.sysunite.com/browse/RC-77, to get row 2
 * counting from Import-ID's, you call getRow(1).
 * 
 * @author alex
 *
 */
public class ABSExcel {

  public enum ABSColumn {
    IMPORTID, OBJECTNAAM, OTLTYPEID, IMPORTIDPARENTABS, OCMSIDPARENTABS, SBSID, OBJECTSTATUS, STATUSX
  }

  private String[][] rawRows;
  private List<ABSRow> rows = new ArrayList<ABSRow>();

  public ABSExcel(String[][] rawRows) {
    this.rawRows = rawRows;
    // Let's try and convert all the String[] into ABSRow's, then check
    // if they are valid, and if so we add them to the array.
    for (String[] rawRow : rawRows) {
      ABSRow absRow = new ABSRow(rawRow);
      if (absRow.isValid())
        rows.add(absRow);
    }
  }

  /**
   * Selects and returns the requested row.
   * 
   * @param row
   *          which row to return.
   * @return an ABSRow containing all the data from the selected row.
   */
  public ABSRow getRow(int row) {
    return rows.get(row);
  }

  /**
   * Counts and returns the number of rows of data. Note: this is excluding the
   * header row with data.
   * 
   * @return the total number of rows of data.
   */
  public int getRowCount() {
    return rows.size();
  }

  /**
   * Retrieves a single value from an intersection in this Excel sheet.
   * 
   * @param type
   *          the y-value of the intersection.
   * @param row
   *          the x-value of the intersection.
   * @return data that is stored in that particular cell in this ABSExcel sheet.
   */
  public String getValue(ABSColumn type, int row) {
    return rawRows[row][type.ordinal()];
  }

  /**
   * Retrieves non-empty values from columns as selected in the where parameter of
   * an ABSExcel sheet.
   * 
   * @param excel
   *          the ABSExcel sheet to read through.
   * @param where
   *          the condition to meet, read as; which non-empty values should be
   *          returned from what column.
   * @param filter
   *          indication to filter non-empty values. True to filter, false to
   *          incluse empty values.
   * @return
   */
  public String[] getValues(ABSColumn where, boolean filter) {
    List<String> data = new ArrayList<String>();
    // We do add an empty value tho for this header field,
    // but only if filter is not set.
    // This way we stay true to it's design principle.
    if (!filter)
      data.add("");
    for (int i = 0; i < getRowCount(); i++) {
      String value = getValue(where, i);
      if (filter && value.isEmpty()) {
        continue;
      }
      data.add(value);
    }
    return data.toArray(new String[] {});
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String[] row : rawRows) {
      for (String str : row) {
        sb.append(str + "--");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Small inner class to replace String[] type for rows in an ABSExcel.
   * 
   * @author alex
   *
   */
  public class ABSRow {
    private String[] row;

    public ABSRow(String[] row) {
      this.row = row;
    }

    /**
     * Retrieve a value from a column in this row.
     * 
     * @param column
     * @return
     */
    public String getValue(ABSColumn column) {
      return row[column.ordinal()];
    }

    /**
     * Helper method to quickly identify this rows import id.
     * 
     * @return
     */
    public int getImportId() {
      return Integer.parseInt(getValue(ABSColumn.IMPORTID));
    }

    /**
     * Checks if this ABSRow is a valid one. A valid row is one where the following
     * columns are set: ImportID (and is a number), Objectnaam, OTL type ID,
     * Objectstatus and Status x, and ImportID parent ABS && OCMSId parent ABS
     * aren't both set at the same time.
     * 
     * @return true if the ABSRow is valid, false otherwise.
     */
    public boolean isValid() {
      try {
        // Try to get the ImportID, if it fails (when text is entered) a
        // NumberFormatException is thrown. This happens in the case of the
        // header column and wrongly entered data in the excel sheet.
        getImportId();
        return !getValue(ABSColumn.IMPORTID).isEmpty() && !getValue(ABSColumn.OBJECTNAAM).isEmpty()
            && !getValue(ABSColumn.OTLTYPEID).isEmpty()
            && !(!getValue(ABSColumn.IMPORTIDPARENTABS).isEmpty() && !getValue(ABSColumn.OCMSIDPARENTABS).isEmpty());
      } catch (NumberFormatException nfe) {
        return false;
      }
    }
  }

}
