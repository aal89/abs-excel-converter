package com.weaverplatform.absexcelconverter.util.wo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.weaverplatform.absexcelconverter.util.Props;
import com.weaverplatform.absexcelconverter.util.workbook.Workbook;
import com.weaverplatform.absexcelconverter.util.workbook.model.ABSExcel;
import com.weaverplatform.absexcelconverter.util.workbook.model.ABSExcel.ABSColumn;
import com.weaverplatform.absexcelconverter.util.workbook.model.ABSExcel.ABSRow;
import com.weaverplatform.protocol.WeaverError;
import com.weaverplatform.protocol.model.AttributeDataType;
import com.weaverplatform.protocol.model.CreateAttributeOperation;
import com.weaverplatform.protocol.model.CreateNodeOperation;
import com.weaverplatform.protocol.model.CreateRelationOperation;
import com.weaverplatform.protocol.model.WriteOperation;

/**
 * Object used to parse Weaver WriteOperations from Excel Workbook objects.
 * 
 * @author alex
 *
 */
public class WriteOperationParser {

  private static JsonParser jsonParser = new JsonParser();
  public static String DEFAULT_USERNAME = Props.get("parser.default_name");

  /**
   * Parses WriteOperation's based on a provided Weaver Workbook object. It tries
   * to extract an ABSExcel object from that Workbook which forms a base for the
   * WriteOperation objects. When no valid ABSExcel can be extracted, it will
   * throw a WeaverError.
   * 
   * @param workbook
   *          an Weaver Workbook.
   * @return an array containing Weaver WriteOperation.
   */
  public synchronized static WriteOperation[] parseOperations(Workbook workbook) {
    try {
      // First things first extract the ABSExcel from the provided Workbook
      ABSExcel excel = workbook.read();
      // If that succeeded allocate and setup an operations ArrayList which
      // later on will be converted to a WriteOperation array.
      List<WriteOperation> operations = new ArrayList<WriteOperation>();
      // A map to keep track of all created PreparedWriteOperations
      // Sorted on their ImportID integers.
      Map<Integer, PreparedWriteOperation> pwos = new HashMap<Integer, PreparedWriteOperation>();
      // Loop through all the rows in the ABSExcel document.
      for (int i = 0; i < excel.getRowCount(); i++) {
        // And create an PreparedWriteOperation for it, this way we will generate
        // UUIDs for it and we can use these later for dependent rows.
        PreparedWriteOperation pwo = new PreparedWriteOperation(excel.getRow(i));
        // Add the created pwo to the map.
        pwos.put(pwo.getImportId(), pwo);
        // If the pwo is dependant we first have to get the UUID of the parent
        // row and use that one to generate the rest of the WO, which we later insert
        // into the operations list.
        if (pwo.isDependant()) {
          // Easy check, if it's an OCMS parent ID, we just set that as the source.
          // Otherwise we'll retrieve the nodeId from the mentioned pwo.
          switch (pwo.getDependantType()) {
          case IMPORTIDPARENTABS:
            // In this particular case we are sure (bcuz of the switch) that the
            // getDependantId() will return an Integer wrapped in a String object
            // So we parse it to an Integer.
            int dependantId = Integer.parseInt(pwo.getDependantId());
            // Check if the dependant pwo exists in the pwos collection. If not
            // we will throw a WeaverError stating that a error occured.
            if (!pwos.containsKey(dependantId))
              throw new WeaverError(345,
                  "Invalid PreparedWriteOperation. Check ImportID: " + pwo.getImportId() + " for errors.");
            String nodeIdFromParent = pwos.get(dependantId).getNodeId();
            pwo.setTarget(nodeIdFromParent, pwos.get(dependantId).getRow().getValue(ABSColumn.OTLTYPEID));
            break;
          case OCMSIDPARENTABS:
            // This is just a CMDB (other name for OCMS) target id, no need for lookup, just
            // set it.
            pwo.setTarget(pwo.getDependantId());
            break;
          }
        }
        // After this we generate the WO's from the pwo and insert them into the
        // operations list.
        for (WriteOperation wo : pwo.toWriteOperations()) {
          operations.add(wo);
        }
      }
      // When that is all done we convert the ArrayList to a normal array
      // and return it to the user.
      return operations.toArray(new WriteOperation[] {});
    } catch (WeaverError we) {
      // Rethrow the error
      throw we;
    }
  }

  /**
   * Parses WriteOperation's based on a provided Weaver Workbook object. It tries
   * to extract an ABSExcel object from that Workbook which forms a base for the
   * WriteOperation objects. When no valid ABSExcel can be extracted, it will
   * throw a WeaverError.
   * 
   * @param workbook
   *          an Weaver Workbook.
   * @return a String containing all the WriteOperations in Json format.
   */
  public synchronized static String parseArray(Workbook workbook) {
    JsonArray array = new JsonArray();
    WriteOperation[] operations = parseOperations(workbook);

    for (WriteOperation wo : operations) {
      JsonObject json = jsonParser.parse(wo.toJson()).getAsJsonObject();
      array.add(json);
    }

    return array.toString();
  }

  /**
   * Inner class which 'prepares' an ABSRow to be converted into a WriteOperation.
   * Generally it only generates an UUID beforehand.
   * 
   * @author alex
   *
   */
  public static class PreparedWriteOperation implements Dependency {

    private String nodeId;
    /**
     * This targetId is a hint for this PreparedWriteOperation to indicate it is a
     * dependancy on another node. So when this targetId is set, we actually create
     * a relation to this other node with a keyname otl:hasPart.
     */
    private String targetId;
    private String targetOtlType;
    private ABSRow row;

    public PreparedWriteOperation(ABSRow row) {
      this.row = row;
      this.nodeId = generateUUID();
    }

    /**
     * Delegate method.
     * 
     * @return the Import-ID belonging to this row.
     */
    public int getImportId() {
      return getRow().getImportId();
    }

    public ABSRow getRow() {
      return row;
    }

    public String getNodeId() {
      return nodeId;
    }

    /**
     * Use this method in order to indicate a relation between ImportID's in the
     * excel sheet.
     * 
     * @param targetId
     *          id from the node from the target.
     * @param targetOtlType
     *          the otl type id belonging to this target.
     */
    public void setTarget(String targetId, String targetOtlType) {
      this.targetId = targetId;
      this.targetOtlType = targetOtlType;
    }

    /**
     * Use this method in order to indicate a relation between an ImportId and an
     * OCMS parent.
     * 
     * @param targetId
     *          the id from the target in the OCMS scheme.
     */
    public void setTarget(String targetId) {
      this.targetId = targetId;
    }

    /**
     * Generates all the WriteOperation's which should semantically describe this
     * PreparedWriteOperation's ABSRow. This method operates as follows, for the
     * ABSRow it holds it: <br>
     * 1st: Creates an new node. <br>
     * 2nd: It creates three attributes, named: hasName, objectStatus, statusX. <br>
     * 3rd: Create one or two relations, named: rdf:type, hasSBS (optional). <br>
     * 4th: Check if the inner targetId is set. If so: <br>
     * 4a: Create extra nodes and relations according to the OTL-standard. <br>
     * 4b: Otherwise don't create the extra relation. <br>
     * 5th: Return an array containing all these WriteOperations.
     * 
     * @return a WriteOperation array containing all the WriteOperation's which
     *         should cover this PreparedWriteOperation object.
     */
    public WriteOperation[] toWriteOperations() {
      List<WriteOperation> operations = new ArrayList<WriteOperation>();
      String otlTypeId = getRow().getValue(ABSColumn.OTLTYPEID);
      operations.add(createNodeOperation(nodeId));
      operations.add(createAttributeOperation(generateUUID(), nodeId, "hasName", getRow().getValue(ABSColumn.OBJECTNAAM)));
      operations.add(createAttributeOperation(generateUUID(), nodeId, "objectStatus", getRow().getValue(ABSColumn.OBJECTSTATUS)));
      operations.add(createAttributeOperation(generateUUID(), nodeId, "statusX", getRow().getValue(ABSColumn.STATUSX)));
      operations.add(createRelationOperation(generateUUID(), nodeId, "rdf:type", otlTypeId));
      // The only optional field, check if it's set.
      if (!getRow().getValue(ABSColumn.SBSID).isEmpty())
        operations.add(createRelationOperation(generateUUID(), nodeId, "hasSBS", getRow().getValue(ABSColumn.SBSID)));
      // Check if targetId is set. That means it involves some special node
      // creation/manipulation we want to comply to the OTL-standard. It
      // involves creating a separate node (intermediate) which holds three relations.
      // One to a new created node with an id gotten from the mini.json.
      // One to the sourceId (this.nodeId) with key: otl:hasPart.
      // One to the targetId with key: otl:hasAssembly.
      if (targetId != null && targetOtlType != null) {
        String intermediateNodeId = generateUUID();
        // First create this intermediate node
        operations.add(createNodeOperation(intermediateNodeId));
        // Create the two relation between the intermediate node and the
        // sourceId and targetId.
        operations.add(createRelationOperation(generateUUID(), intermediateNodeId, "otl:hasAssembly", targetId));
        operations.add(createRelationOperation(generateUUID(), intermediateNodeId, "otl:hasPart", nodeId));
        // Then create another node with the ID which originates from the
        // mini.json in the resources folder.
        String nodeIdFromAggregations = AggregationResolver.getInstance().lookup(otlTypeId, targetOtlType);
        operations.add(createNodeOperation(nodeIdFromAggregations));
        // Finally create the relation between this intermediate node and
        // this just created node above.
        operations.add(createRelationOperation(generateUUID(), intermediateNodeId, "rdf:type", nodeIdFromAggregations));
      } else if(targetId != null && targetOtlType == null) {
        // This case happens when the OCMSid parent was set and not the ImportId parent ABS
        operations.add(createRelationOperation(generateUUID(), nodeId, "TODO", targetId));
        // TODO: this type of parent relation eventually will also get the same implementation of
        // the ImportID parent ABS implementation (see above), only I need to get another .csv file
        // in which the mapping from this OCMSID parent ABS to the aggregation id is.
        // TODO: make such a intermediate node as above and stuff...
        // TODO: give the same key names as above implementation
      }
      return operations.toArray(new WriteOperation[] {});
    }

    private WriteOperation createNodeOperation(String nodeId) {
      return new CreateNodeOperation(DEFAULT_USERNAME, nodeId);
    }

    private WriteOperation createAttributeOperation(String nodeId, String sourceId, String key, String value) {
      return new CreateAttributeOperation(DEFAULT_USERNAME, nodeId, sourceId, key, value, AttributeDataType.STRING);
    }

    private WriteOperation createRelationOperation(String uuid, String sourceId, String key, String targetId) {
      return new CreateRelationOperation(DEFAULT_USERNAME, uuid, sourceId, key, targetId);
    }

    private String generateUUID() {
      return UUID.randomUUID().toString();
    }

    @Override
    public boolean isDependant() {
      return !(row.getValue(ABSColumn.IMPORTIDPARENTABS).isEmpty()
          && row.getValue(ABSColumn.OCMSIDPARENTABS).isEmpty());
    }

    @Override
    public String getDependantId() {
      String value = null;
      if (isDependant()) {
        switch (getDependantType()) {
        case IMPORTIDPARENTABS:
          value = row.getValue(ABSColumn.IMPORTIDPARENTABS);
          break;
        case OCMSIDPARENTABS:
          value = row.getValue(ABSColumn.OCMSIDPARENTABS);
          break;
        }
      }
      return value;
    }

    @Override
    public WriteOperationType getDependantType() {
      WriteOperationType value = null;
      if (isDependant()) {
        // Either one is filled in, never both. Return the appropriate value.
        if (row.getValue(ABSColumn.IMPORTIDPARENTABS).isEmpty()) {
          value = WriteOperationType.OCMSIDPARENTABS;
        } else {
          value = WriteOperationType.IMPORTIDPARENTABS;
        }
      }
      return value;
    }
  }
}
