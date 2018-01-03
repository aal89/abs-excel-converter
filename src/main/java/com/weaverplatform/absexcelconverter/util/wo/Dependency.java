package com.weaverplatform.absexcelconverter.util.wo;

/**
 * Interface to determine said object is dependant on something else, which
 * should be resolved first. This dependency is primarily meant for
 * PreparedWriteOperations.
 * 
 * @author alex
 *
 */
public interface Dependency {

  public enum WriteOperationType {
    IMPORTIDPARENTABS, OCMSIDPARENTABS
  }

  /**
   * Indicates wheter this object is dependant on any other object.
   * 
   * @return true if it is, false otherwise.
   */
  boolean isDependant();

  /**
   * Returns either the [ImportID parent ABS] column or the [OCMS-ID parent ABS]
   * column, depending on which one is set.
   * 
   * @return a String representation of the ID. An empty String is returned when
   *         this WriteOperation isn't dependant.
   */
  String getDependantId();

  /**
   * Decides wheter this dependency is of type [ImportID parent ABS] or [OCMS-ID
   * parent ABS] as the columns are named in an ABSRow.
   * 
   * @return an WriteOperationType to indicate one column and another
   *         WriteOperationType to indicate another column.
   */
  WriteOperationType getDependantType();

}
