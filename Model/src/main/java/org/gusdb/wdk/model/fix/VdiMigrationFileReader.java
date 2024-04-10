package org.gusdb.wdk.model.fix;

import com.fasterxml.jackson.databind.JsonNode;
import org.gusdb.fgputil.json.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VdiMigrationFileReader {
  private File file;

  public VdiMigrationFileReader(File file) {
    this.file = file;
  }

  /**
   * Parse the tinydb file into a map of legacy UD identifiers to VDI identifiers.
   *
   * Example file format:
   *
   * {
   *   "_default": {
   *     "1": {
   *       "type": "owner",
   *       "udId": 1234,
   *       "vdiId": "123XyZ",
   *       "msg": null,
   *       "time": "Fri Mar 26 00:00:00 2024"
   *     }
   *  }
   *
   * @return Map of legacy UD Ids to VDI Ids.
   */
  public Map<String, String> readLegacyStudyIdToVdiId() {
    try {
      JsonNode root = JsonUtil.Jackson.readTree(file);
      JsonNode dbRoot = root.get("_default");

      Map<String, String> mapping = new HashMap<>();
      Iterator<Map.Entry<String, JsonNode>> fieldIterator = dbRoot.fields();

      // Iterate through each field in the "_default" node.
      // Ignore the numeric index keys and extract the udId and vdiId fields to create mapping.
      while (fieldIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = fieldIterator.next();
        mapping.put(entry.getValue().get("udId").asText(), entry.getValue().get("vdiId").asText());
      }

      return mapping;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}