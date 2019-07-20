package org.gusdb.wdk.model.toolbundle;

import java.io.IOException;

import org.gusdb.wdk.model.WdkRuntimeException;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@FunctionalInterface
public interface ColumnToolConfig {

  JsonNode getConfig();

  /**
   * @return org.json version of the config object
   */
  default JSONObject getConfigAsJSONObject() {
    try {
      return new JSONObject(new ObjectMapper().writeValueAsString(getConfig()));
    }
    catch (JsonProcessingException | JSONException e) {
      throw new WdkRuntimeException("Unable to deserialize (using Jackson) or serialize (using org.json) JSON node: " + getConfig());
    }
  }

  default ColumnToolConfig deepCopy() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode copy = mapper.readTree(mapper.writeValueAsString(getConfig()));
      return () -> copy;
    }
    catch (IOException e) {
      throw new WdkRuntimeException("Unable to deserialize (using Jackson) or serialize (using org.json) JSON node: " + getConfig());
    }
    
  }

}
