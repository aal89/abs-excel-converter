package com.weaverplatform.absexcelconverter.util.wo;

import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.weaverplatform.protocol.WeaverError;

/**
 * Aggregation between relations of Weaver node's which coher to the
 * OTL-standard have a special identifier. This object is used to lookup this
 * identifier based on sourceId and targetId from that particular relation.
 * 
 * It is a singleton by implementation this way it is for sure the mini.json
 * does not get loaded unnecessarily much into memory.
 * 
 * This class is based on the relation-aggregations-mini.json file which should
 * be in the resources folder.
 * 
 * @author alex
 *
 */
public class AggregationResolver {

  private static AggregationResolver instance = null;
  private volatile JsonArray aggregations;
  private volatile JsonArray mapping;

  private AggregationResolver() {
    try {
      JsonParser parser = new JsonParser();
      String aggregationData = Resources.toString(Resources.getResource("relation-aggregations-mini.json"),
          Charset.defaultCharset());
      String mappingData = Resources.toString(Resources.getResource("ocms-otl-mapping-mini.json"),
          Charset.defaultCharset());
      JsonElement element = parser.parse(aggregationData);
      aggregations = element.getAsJsonArray();
      element = parser.parse(mappingData);
      mapping = element.getAsJsonArray();
    } catch (IOException e1) {
      throw new WeaverError(-1, "Failed to load the aggregations relation mapping or the ocms mapping.");
    }
  }

  public static AggregationResolver getInstance() {
    if (instance == null) {
      instance = new AggregationResolver();
    }
    return instance;
  }

  /**
   * In essence the same as lookup() but performs one more search before runnning
   * lookup(). It will try and find the OTL type ID that belongs to this OCMSId.
   * Then when it has a match runs the lookup to return the proper aggregation id.
   * 
   * @param id the OCMSId.
   * @return a String containing the aggregation id of the relation.
   */
  public synchronized String lookupFromOCMS(String sourceOtlId, String id) {
    String targetId = retrieveOtlTypeId(id).get(1).toString().replaceAll("\"", "").replace("otl:", "");
    return lookup(sourceOtlId, targetId);
  }

  /**
   * Finds and returns the aggregation id of the given relation.
   * 
   * @param sourceId
   *          the id of the right side of the relation.
   * @param targetId
   *          the id of the left side of the relation.
   * @return a String containing the aggregation id of the relation.
   */
  public synchronized String lookup(String sourceOtlId, String targetOtlId) {
    return retrieveAggregationId(sourceOtlId, targetOtlId).get(2).toString().replaceAll("\"", "");
  }

  /**
   * Loops through the volatile dataset containing all the aggregation id's to
   * find the id matching the sourceId and targetId.
   * 
   * @param sourceId
   *          the id of the left side of the relation.
   * @param targetId
   *          the id of the rigth side of the relation.
   * @return a JsonArray containing the following info: [0]: the left side
   *         (source)id, [1]: the right side (target)id, [2]: the aggregation is.
   */
  private JsonArray retrieveAggregationId(String sourceOtlId, String targetOtlId) {
    for (JsonElement element : aggregations) {
      JsonArray ele = element.getAsJsonArray();
      if (ele.get(0).getAsString().equals("otl:" + sourceOtlId) && ele.get(1).getAsString().equals("otl:" + targetOtlId))
        return ele;
    }
    throw new WeaverError(-1,
        "No aggregation id found for this relation (source/target): " + sourceOtlId + " " + targetOtlId);
  }
  
  /**
   * Loops through the volatile dataset containing all the mapping otl id's to
   * find the id matching the sourceId and OCMSId.
   * 
   * @param sourceId
   *          the id of the left side of the relation.
   * @param targetId
   *          the id of the right side of the relation.
   * @return a JsonArray containing the following info: [0]: the left side
   *         (source)id, [1]: the right side (target)id, [2]: the aggregation is.
   */
  private JsonArray retrieveOtlTypeId(String OCMSId) {
    for (JsonElement element : mapping) {
      JsonArray ele = element.getAsJsonArray();
      if (ele.get(0).getAsString().equals(OCMSId))
        return ele;
    }
    throw new WeaverError(-1,
        "No otl type id found for this ocms: " + OCMSId);
  }

}
